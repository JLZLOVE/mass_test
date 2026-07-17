package untiy.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import untiy.annotation.IgnoreAuthConstants;
import untiy.config.IgnoreAuthRegistry;
import untiy.security.LoginUserDetails;
import untiy.security.UserCacheHelper;
import untiy.service.AuthorService;
import untiy.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * JWT 认证过滤器。
 * <p>
 * 流程：静态资源白名单 → {@link untiy.annotation.IgnoreAuth} 路径匹配
 * → Token 校验 → Redis {@code user:v2:{username}} 必须存在 → SecurityContext。
 * <p>
 * Redis 缓存缺失时直接 401（不回源数据库），与注销删缓存语义一致。
 */
@Component
@EnableConfigurationProperties(IgnorePathsProperties.class)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private static final String UNAUTHORIZED_JSON = "{\"code\":401,\"msg\":\"未登录或Token无效\"}";

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IgnorePathsProperties ignorePathsProperties;

    @Autowired
    private IgnoreAuthRegistry ignoreAuthRegistry;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String lookupPath = ignoreAuthRegistry.resolveLookupPath(request);
        log.debug("JwtFilter 拦截请求：{} {}", request.getMethod(), lookupPath);

        if (isIgnoredPath(lookupPath)) {
            chain.doFilter(request, response);
            return;
        }

        if (shouldIgnoreAuth(request)) {
            request.setAttribute(IgnoreAuthConstants.REQUEST_ATTR, Boolean.TRUE);
            setPublicAuthentication();
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            token = request.getHeader("Token");
        }

        if (!jwtTokenUtil.validateToken(token)) {
            sendUnauthorizedJson(response);
            return;
        }

        String username;
        try {
            username = jwtTokenUtil.getUsernameFromToken(token);
        } catch (JwtException e) {
            log.warn("Token 解析失败：{} {}，原因：{}", request.getMethod(), lookupPath, e.getMessage());
            sendUnauthorizedJson(response);
            return;
        }

        if (username == null || username.trim().isEmpty()) {
            sendUnauthorizedJson(response);
            return;
        }

        String cacheKey = UserCacheHelper.keyForUsername(username);
        LoginUserDetails loginUser = readFromCache(cacheKey);
        if (loginUser == null) {
            log.debug("用户 {} Redis 会话不存在或已失效，拒绝访问 {}", username, lookupPath);
            sendUnauthorizedJson(response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
        chain.doFilter(request, response);
    }

    private LoginUserDetails readFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                return null;
            }
            LoginUserDetails.CacheSnapshot snapshot = parseCacheSnapshot(cached);
            if (snapshot != null) {
                return LoginUserDetails.fromCacheSnapshot(snapshot, authorService);
            }
            if (cached instanceof Collection) {
                log.info("Redis Key {} 为旧版权限集合缓存，删除", cacheKey);
                redisTemplate.delete(cacheKey);
            }
        } catch (Exception e) {
            log.warn("读取 Redis 用户缓存失败，Key={}，删除脏数据", cacheKey, e);
            redisTemplate.delete(cacheKey);
        }
        return null;
    }

    private LoginUserDetails.CacheSnapshot parseCacheSnapshot(Object cached) {
        if (cached instanceof LoginUserDetails.CacheSnapshot) {
            return (LoginUserDetails.CacheSnapshot) cached;
        }
        if (cached instanceof Map) {
            return objectMapper.convertValue(cached, LoginUserDetails.CacheSnapshot.class);
        }
        return null;
    }

    private boolean shouldIgnoreAuth(HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute(IgnoreAuthConstants.REQUEST_ATTR))) {
            return true;
        }
        return ignoreAuthRegistry.matches(request);
    }

    private void setPublicAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("public", null, Collections.emptyList()));
    }

    private void sendUnauthorizedJson(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(UNAUTHORIZED_JSON);
    }

    private boolean isIgnoredPath(String lookupPath) {
        if (ignorePathsProperties == null || ignorePathsProperties.getUrls() == null) {
            return false;
        }
        for (String pattern : ignorePathsProperties.getUrls()) {
            if (pathMatcher.match(pattern, lookupPath)) {
                return true;
            }
        }
        return false;
    }
}
