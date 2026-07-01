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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import untiy.annotation.IgnoreAuth;
import untiy.exception.EIException;
import untiy.service.impl.UserDetailServiceImpl;
import untiy.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器。
 * <p>
 * 流程：静态资源白名单 → {@link IgnoreAuth} 注解检查 → Token 校验 → Redis 权限加载 → SecurityContext。
 * <p>
 * 业务接口是否免登录由 {@link IgnoreAuth} 控制，不再依赖 /login/** 等路径白名单。
 */
@Component
@EnableConfigurationProperties(IgnorePathsProperties.class)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private static final String REDIS_USER_KEY_PREFIX = "user:";

    private static final long CACHE_TTL_HOURS = 1L;

    private static final String UNAUTHORIZED_JSON = "{\"code\":401,\"msg\":\"未登录或Token无效\"}";

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private IgnorePathsProperties ignorePathsProperties;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String requestURI = request.getServletPath();
        log.debug("JwtFilter 拦截请求：{}", requestURI);

        // 1. Swagger / 静态资源白名单（与 SecurityConfig、application.yml 保持一致）
        if (isIgnoredPath(requestURI)) {
            log.debug("路径 {} 在静态白名单内，直接放行", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // 2. @IgnoreAuth 注解检查：方法或类上标注则免 Token
        if (isIgnoreAuthRequest(request)) {
            log.debug("路径 {} 标注 @IgnoreAuth，免 Token 放行", requestURI);
            setPublicAuthentication();
            chain.doFilter(request, response);
            return;
        }

        // 3. 无注解接口：校验 Token
        String token = request.getHeader("Token");
        if (token == null || token.trim().isEmpty()) {
            log.warn("请求缺少 Token，路径：{}", requestURI);
            sendUnauthorizedJson(response);
            return;
        }

        if (!jwtTokenUtil.validateToken(token)) {
            if (jwtTokenUtil.isTokenExpired(token)) {
                log.warn("Token 已过期，路径：{}", requestURI);
            } else {
                log.warn("Token 无效，路径：{}", requestURI);
            }
            sendUnauthorizedJson(response);
            return;
        }

        // 4. 解析 username
        String username;
        try {
            username = jwtTokenUtil.getUsernameFromToken(token);
        } catch (JwtException e) {
            log.warn("Token 解析失败，路径：{}，原因：{}", requestURI, e.getMessage());
            sendUnauthorizedJson(response);
            return;
        }

        if (username == null || username.trim().isEmpty()) {
            log.warn("Token 中未包含有效 username，路径：{}", requestURI);
            sendUnauthorizedJson(response);
            return;
        }

        // 5. Redis 读取权限，未命中则数据库回源
        String cacheKey = REDIS_USER_KEY_PREFIX + username;
        Collection<? extends GrantedAuthority> authorities = loadAuthoritiesFromCache(cacheKey);

        if (authorities == null) {
            log.debug("用户 {} 权限缓存未命中，从数据库加载", username);
            try {
                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                authorities = userDetails.getAuthorities();
                redisTemplate.opsForValue().set(cacheKey, authorities, CACHE_TTL_HOURS, TimeUnit.HOURS);
                log.info("用户 {} 权限已从数据库加载并写入缓存", username);
            } catch (UsernameNotFoundException e) {
                log.warn("用户 {} 不存在", username);
                sendUnauthorizedJson(response);
                return;
            } catch (EIException e) {
                log.warn("用户 {} 加载失败：{}", username, e.getMessage());
                sendUnauthorizedJson(response);
                return;
            } catch (Exception e) {
                log.error("加载用户 {} 权限时发生系统异常", username, e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"系统异常\"}");
                return;
            }
        } else {
            log.debug("用户 {} 权限缓存命中", username);
        }

        // 6. 写入 SecurityContext 并放行
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("用户 {} 认证成功，继续执行过滤器链", username);
        chain.doFilter(request, response);
    }

    /**
     * 通过 HandlerMapping 判断目标接口是否标注 {@link IgnoreAuth}。
     * 若解析失败（如 404），返回 false，继续 Token 校验。
     */
    private boolean isIgnoreAuthRequest(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = handlerMapping.getHandler(request);
            if (chain == null || !(chain.getHandler() instanceof HandlerMethod)) {
                return false;
            }
            HandlerMethod handler = (HandlerMethod) chain.getHandler();
            if (handler.getMethodAnnotation(IgnoreAuth.class) != null) {
                return true;
            }
            return handler.getBeanType().getAnnotation(IgnoreAuth.class) != null;
        } catch (Exception e) {
            log.debug("解析 Handler 失败，继续 Token 校验：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 为 @IgnoreAuth 接口设置占位认证，满足 Security anyRequest().authenticated()。
     */
    private void setPublicAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("public", null, Collections.emptyList())
        );
    }

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
            log.warn("Redis 缓存 Key {} 内容类型不匹配，将回源数据库", cacheKey);
        } catch (Exception e) {
            log.warn("读取 Redis 权限缓存失败，Key={}，将回源数据库", cacheKey, e);
        }
        return null;
    }

    /**
     * 返回 401 JSON（必须设置 Content-Type: application/json）。
     */
    private void sendUnauthorizedJson(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(UNAUTHORIZED_JSON);
    }

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
