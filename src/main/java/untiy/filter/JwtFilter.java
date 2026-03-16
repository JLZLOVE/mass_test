package untiy.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import untiy.service.impl.UserDetailServiceImpl;
import untiy.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class JwtFilter extends OncePerRequestFilter {  // 必须继承 OncePerRequestFilter

    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader("Token");
        if (token != null && jwtTokenUtil.validateToken(token)) {
            String userId = jwtTokenUtil.getUserIdFromToken(token);
            Collection<GrantedAuthority> authorities;

            // 1. 先从 Redis 获取用户权限
            authorities = (Collection<GrantedAuthority>) redisTemplate.opsForValue().get("user:" + userId);

            if (authorities == null) {
                // 2. Redis 未命中，从数据库加载
                UserDetails userDetails = userDetailService.loadUserByUsername(userId);
                authorities = (Collection<GrantedAuthority>) userDetails.getAuthorities();
                // 3. 存入 Redis 以备后续使用（设置过期时间）
                redisTemplate.opsForValue().set("user:" + userId, authorities, 1, TimeUnit.HOURS);
            }

            // 4. 构建 Authentication 对象（这里 principal 使用 userId，后续可通过 userId 获取用户信息）
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        // 5. 继续执行过滤器链
        chain.doFilter(request, response);
    }
}