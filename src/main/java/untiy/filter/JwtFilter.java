package untiy.filter;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import untiy.exception.EIException;
import untiy.service.impl.UserDetailServiceImpl;
import untiy.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器。
 * <p>
 * 流程：白名单放行 → 提取 Header「Token」→ 校验 JWT → 解析 username →
 * Redis 读取权限（未命中则数据库回源并补写缓存）→ 写入 SecurityContext → 放行。
 * <p>
 * Redis Key 规范：{@code user:{username}}，值为 {@code Collection<GrantedAuthority>}。
 */
@Component
@EnableConfigurationProperties(IgnorePathsProperties.class)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    /** Redis 权限缓存 Key 前缀，完整 Key 为 user:{username} */
    private static final String REDIS_USER_KEY_PREFIX = "user:";

    /** 权限缓存过期时间（小时），与登录接口保持一致 */
    private static final long CACHE_TTL_HOURS = 1L;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private IgnorePathsProperties ignorePathsProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        // 使用 getServletPath() 获取相对路径（已剥离 context-path，/Mass_Test）
        String requestURI = request.getServletPath();
        log.debug("JwtFilter 拦截请求：{}", requestURI);

        // 1. 白名单路径直接放行（路径来自 security.ignore.urls 配置）
        if (isIgnoredPath(requestURI)) {
            log.debug("路径 {} 在白名单内，直接放行", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // 2. 从 Header 提取 Token（前端通过 Header「Token」传递）
        String token = request.getHeader("Token");
        if (token == null || token.trim().isEmpty()) {
            log.warn("请求缺少 Token，路径：{}", requestURI);
            sendUnauthorized(response, "认证失败");
            return;
        }

        // 3. 校验 Token 签名与有效期
        if (!jwtTokenUtil.validateToken(token)) {
            if (jwtTokenUtil.isTokenExpired(token)) {
                log.warn("Token 已过期，路径：{}", requestURI);
            } else {
                log.warn("Token 无效，路径：{}", requestURI);
            }
            sendUnauthorized(response, "认证失败");
            return;
        }

        // 4. 解析 username（学号/工号），subject 中禁止存储数据库主键 ID
        String username;
        try {
            username = jwtTokenUtil.getUsernameFromToken(token);
        } catch (JwtException e) {
            log.warn("Token 解析失败，路径：{}，原因：{}", requestURI, e.getMessage());
            sendUnauthorized(response, "认证失败");
            return;
        }

        if (username == null || username.trim().isEmpty()) {
            log.warn("Token 中未包含有效 username，路径：{}", requestURI);
            sendUnauthorized(response, "认证失败");
            return;
        }

        // 5. 从 Redis 读取权限集合（Key：user:{username}）
        String cacheKey = REDIS_USER_KEY_PREFIX + username;
        Collection<? extends GrantedAuthority> authorities = loadAuthoritiesFromCache(cacheKey);

        if (authorities == null) {
            log.debug("用户 {} 权限缓存未命中，从数据库加载", username);
            // 6. 缓存未命中，从数据库回源并补写 Redis
            try {
                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                authorities = userDetails.getAuthorities();
                redisTemplate.opsForValue().set(cacheKey, authorities, CACHE_TTL_HOURS, TimeUnit.HOURS);
                log.info("用户 {} 权限已从数据库加载并写入缓存", username);
            } catch (UsernameNotFoundException e) {
                log.warn("用户 {} 不存在", username);
                sendUnauthorized(response, "认证失败");
                return;
            } catch (EIException e) {
                // UserDetailServiceImpl 在用户不存在时抛出 EIException，统一按认证失败处理
                log.warn("用户 {} 加载失败：{}", username, e.getMessage());
                sendUnauthorized(response, "认证失败");
                return;
            } catch (Exception e) {
                log.error("加载用户 {} 权限时发生系统异常", username, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统异常");
                return;
            }
        } else {
            log.debug("用户 {} 权限缓存命中", username);
        }

        // 7. 构建 Authentication 并写入 SecurityContext（principal 使用 username，credentials 为 null）
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("用户 {} 认证成功，继续执行过滤器链", username);
        chain.doFilter(request, response);
    }

    /**
     * 从 Redis 读取权限集合。
     * 若反序列化结果不是 GrantedAuthority 集合（如旧格式缓存），返回 null 触发数据库回源。
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> loadAuthoritiesFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (!(cached instanceof Collection)) {
                return null;
            }
            Collection<?> collection = (Collection<?>) cached;
            if (collection.isEmpty()) {
                return (Collection<? extends GrantedAuthority>) collection;
            }
            Object first = collection.iterator().next();
            if (first instanceof GrantedAuthority) {
                return (Collection<? extends GrantedAuthority>) collection;
            }
            log.warn("Redis 缓存 Key {} 内容类型不匹配（期望 GrantedAuthority 集合），将回源数据库", cacheKey);
        } catch (Exception e) {
            log.warn("读取 Redis 权限缓存失败，Key={}，将回源数据库", cacheKey, e);
        }
        return null;
    }

    /**
     * 统一返回 401 认证失败响应，禁止返回 403 或泄露堆栈信息。
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /**
     * 判断请求路径是否匹配 security.ignore.urls 中的 Ant 风格通配符。
     */
    private boolean isIgnoredPath(String requestURI) {
        if (ignorePathsProperties == null || ignorePathsProperties.getUrls() == null) {
            return false;
        }
        for (String pattern : ignorePathsProperties.getUrls()) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
