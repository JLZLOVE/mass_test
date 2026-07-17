package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.annotation.IgnoreAuth;
import untiy.annotation.RequiresLevel;
import untiy.exception.Level;
import untiy.security.LoginUserDetails;
import untiy.security.UserCacheHelper;
import untiy.utils.JwtUtil;
import untiy.utils.R;

import java.util.concurrent.TimeUnit;

/**
 * 登录 / 注销接口。
 * <p>
 * POST /login/allocation — 登录：签发 JWT，写入 Redis {@code user:v2:{username}}<br>
 * POST /login/logout — 注销：删除 Redis 会话缓存，后续同 Token 由 JwtFilter 返回 401
 */
@Slf4j
@RestController
@Tag(name = "登录认证", description = "用户登录与注销")
@RequestMapping("/login")
public class LoginController {

    /** 权限缓存过期时间（小时） */
    private static final long CACHE_TTL_HOURS = 1L;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录。
     *
     * @param name     学号/工号（对应 sys_user.username）
     * @param password 密码
     * @return token 与 username，前端通过 Header「Token」携带 JWT 访问受保护接口
     */
    @IgnoreAuth
    @Operation(summary = "用户登录", description = "使用学号/工号和密码进行身份认证，成功返回JWT令牌和用户名")
    @PostMapping("/allocation")
    public R login(@RequestParam String name, @RequestParam String password) {
        log.info("用户 {} 尝试登录", name);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(name, password);

        Authentication authentication = authenticationManager.authenticate(authToken);

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String username = principal.getUsername();

        String token = jwtTokenUtil.generateToken(username);

        String cacheKey = UserCacheHelper.keyForUsername(username);
        if (principal instanceof LoginUserDetails) {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    ((LoginUserDetails) principal).toCacheSnapshot(),
                    CACHE_TTL_HOURS,
                    TimeUnit.HOURS);
        }
        log.info("用户 {} 登录成功，用户快照已缓存至 Redis Key: {}", username, cacheKey);

        return R.ok().put("token", token).put("username", username);
    }

    /**
     * 注销：删除 Redis 会话缓存。不手动 clearContext（由框架在请求结束后清理）。
     * <p>
     * 幂等：上下文无有效 {@link LoginUserDetails} 时仍返回成功。
     */
    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "退出登录", description = "删除当前用户 Redis 会话缓存；需携带有效 Token")
    @PostMapping("/logout")
    public R logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUserDetails)) {
            return R.ok("已退出");
        }
        LoginUserDetails user = (LoginUserDetails) auth.getPrincipal();
        String username = user.getUsername();
        UserCacheHelper.evictByUsername(redisTemplate, username);
        log.info("用户 {} 已退出登录，已清除 Redis 会话缓存", username);
        return R.ok("已退出");
    }
}
