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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import untiy.annotation.IgnoreAuthConstants;
import untiy.config.IgnoreAuthRegistry;
import untiy.exception.EIException;
import untiy.security.LoginUserDetails;
import untiy.security.UserCacheHelper;
import untiy.service.AuthorService;
import untiy.service.impl.UserDetailServiceImpl;
import untiy.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器。
 * <p>
 * 流程：静态资源白名单 → {@link untiy.annotation.IgnoreAuth} 路径匹配（启动扫描注册表）
 * → Token 校验 → Redis 缓存 {@link LoginUserDetails} → SecurityContext。
 */
@Component
@EnableConfigurationProperties(IgnorePathsProperties.class)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private static final long CACHE_TTL_HOURS = 1L;

    private static final String UNAUTHORIZED_JSON = "{\"code\":401,\"msg\":\"未登录或Token无效\"}";

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserDetailServiceImpl userDetailService;

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
        LoginUserDetails loginUser = loadLoginUserDetails(username, cacheKey);
        if (loginUser == null) {
            sendUnauthorizedJson(response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
        chain.doFilter(request, response);
    }

    private LoginUserDetails loadLoginUserDetails(String username, String cacheKey) {
        LoginUserDetails cached = readFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            UserDetails userDetails = userDetailService.loadUserByUsername(username);
            if (!(userDetails instanceof LoginUserDetails)) {
                log.error("loadUserByUsername 未返回 LoginUserDetails：{}", username);
                return null;
            }
            LoginUserDetails loginUser = (LoginUserDetails) userDetails;
            redisTemplate.opsForValue().set(
                    cacheKey, loginUser.toCacheSnapshot(), CACHE_TTL_HOURS, TimeUnit.HOURS);
            return loginUser;
        } catch (UsernameNotFoundException e) {
            log.warn("用户 {} 不存在", username);
            return null;
        } catch (EIException e) {
            log.warn("用户 {} 加载失败：{}", username, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("加载用户 {} 时发生系统异常", username, e);
            return null;
        }
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
                log.info("Redis Key {} 为旧版权限集合缓存，删除并回源", cacheKey);
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
