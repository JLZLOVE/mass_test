package untiy.filter;



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

import java.util.concurrent.TimeUnit;



/**

 * JWT 认证过滤器。

 * <p>

 * 流程：静态资源白名单 → {@link untiy.annotation.IgnoreAuth} 路径匹配（启动扫描注册表）

 * → Token 校验 → Redis 缓存 {@link LoginUserDetails} → SecurityContext。

 * <p>

 * 注意：Filter 位于 DispatcherServlet 之前，不能使用 {@code handlerMapping.getHandler()}，

 * 免登录判断由 {@link IgnoreAuthRegistry} 在应用启动时扫描注解并注册路径。

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

    private AuthorService authorService;



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



        // 1. Swagger / 静态资源白名单

        if (isIgnoredPath(lookupPath)) {

            log.debug("路径 {} 在静态白名单内，直接放行", lookupPath);

            chain.doFilter(request, response);

            return;

        }



        // 2. @IgnoreAuth：启动扫描路径匹配（Filter 阶段可靠方案）

        if (shouldIgnoreAuth(request)) {

            log.debug("@IgnoreAuth 放行：{} {}", request.getMethod(), lookupPath);

            request.setAttribute(IgnoreAuthConstants.REQUEST_ATTR, Boolean.TRUE);


            setPublicAuthentication(); //放入一个已认证的 Authentication

            chain.doFilter(request, response);

            return;

        }



        // 3. 需鉴权：校验 Token

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            // 可降级兼容旧版 Token 头
            token = request.getHeader("Token");
        }



        if (!jwtTokenUtil.validateToken(token)) {

            if (jwtTokenUtil.isTokenExpired(token)) {

                log.warn("Token 已过期：{} {}", request.getMethod(), lookupPath);

            } else {

                log.warn("Token 无效：{} {}", request.getMethod(), lookupPath);

            }

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

            log.warn("Token 中无有效 username：{} {}", request.getMethod(), lookupPath);

            sendUnauthorizedJson(response);

            return;

        }



        String cacheKey = REDIS_USER_KEY_PREFIX + username;

        LoginUserDetails loginUser = loadLoginUserDetails(username, cacheKey);



        if (loginUser == null) {

            sendUnauthorizedJson(response);

            return;

        }



        UsernamePasswordAuthenticationToken authentication =

                new UsernamePasswordAuthenticationToken(

                        loginUser, null, loginUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);



        log.debug("用户 {} 认证成功，有效等级={}", username, loginUser.getEffectiveLevel());

        chain.doFilter(request, response);

    }



    /**

     * 从 Redis 读取 LoginUserDetails 快照；未命中或旧版 Collection 缓存则回源 loadUserByUsername。

     */

    private LoginUserDetails loadLoginUserDetails(String username, String cacheKey) {

        LoginUserDetails cached = readFromCache(cacheKey);

        if (cached != null) {

            log.debug("用户 {} LoginUserDetails 缓存命中", username);

            return cached;

        }



        log.debug("用户 {} 缓存未命中或格式过期，从数据库加载", username);

        try {

            UserDetails userDetails = userDetailService.loadUserByUsername(username);

            if (!(userDetails instanceof LoginUserDetails)) {

                log.error("loadUserByUsername 未返回 LoginUserDetails：{}", username);

                return null;

            }

            LoginUserDetails loginUser = (LoginUserDetails) userDetails;

            redisTemplate.opsForValue().set(

                    cacheKey, loginUser.toCacheSnapshot(), CACHE_TTL_HOURS, TimeUnit.HOURS);

            log.info("用户 {} 详情已加载并写入 Redis", username);

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

            if (cached instanceof LoginUserDetails.CacheSnapshot) {

                return LoginUserDetails.fromCacheSnapshot(

                        (LoginUserDetails.CacheSnapshot) cached, authorService);

            }

            if (cached instanceof Collection) {

                log.info("Redis Key {} 为旧版权限集合缓存，将回源并升级为 LoginUserDetails", cacheKey);

            }

        } catch (Exception e) {

            log.warn("读取 Redis 用户缓存失败，Key={}", cacheKey, e);

        }

        return null;

    }



    /**

     * 判断是否免 Token：优先读请求标记（若已设置），否则走启动扫描的路径注册表。

     */

    private boolean shouldIgnoreAuth(HttpServletRequest request) {

        if (Boolean.TRUE.equals(request.getAttribute(IgnoreAuthConstants.REQUEST_ATTR))) {

            return true;

        }

        return ignoreAuthRegistry.matches(request);

    }



    private void setPublicAuthentication() {

        SecurityContextHolder.getContext().setAuthentication(

                new UsernamePasswordAuthenticationToken("public", null, Collections.emptyList())

        );

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


