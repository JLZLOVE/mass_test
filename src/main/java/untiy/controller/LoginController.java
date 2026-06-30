package untiy.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.service.impl.LoginServiceImpl;
import untiy.utils.JwtUtil;
import untiy.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.TimeUnit;

/**
 * 登录接口。
 * <p>
 * POST /login/allocation?name={学号/工号}&password={密码}
 * <p>
 * 认证成功后：签发 JWT（subject = username）、将权限集合写入 Redis（Key: user:{username}）。
 */



@Slf4j
@RestController
@Tag(name = "登录认证", description = "用户登录接口，获取JWT令牌")
@RequestMapping("/login")
public class LoginController {

    /** Redis 权限缓存 Key 前缀，与 JwtFilter 保持一致 */
    private static final String REDIS_USER_KEY_PREFIX = "user:";

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
    @Operation(summary = "用户登录", description = "使用学号/工号和密码进行身份认证，成功返回JWT令牌和用户名")
    @PostMapping("/allocation")
    public R login(@RequestParam String name, @RequestParam String password) {
        log.info("用户 {} 尝试登录", name);

        // 1. 创建认证令牌 username 传入 Spring Security
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(name, password);

        // 2. 认证（自动调用 UserDetailServiceImpl.loadUserByUsername 并比对密码）
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. 获取认证后的 UserDetails
        LoginServiceImpl userDetails = (LoginServiceImpl) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // 4. 签发 JWT，subject 必须为 username（学号/工号），禁止存储数据库主键 ID
        String token = jwtTokenUtil.generateToken(username);

        // 5. 将权限集合缓存到 Redis，Key: user:{username}，与 JwtFilter 读取规则一致
        String cacheKey = REDIS_USER_KEY_PREFIX + username;
        redisTemplate.opsForValue().set(
                cacheKey,
                userDetails.getAuthorities(),
                CACHE_TTL_HOURS,
                TimeUnit.HOURS
        );
        log.info("用户 {} 登录成功，权限已缓存至 Redis Key: {}", username, cacheKey);

        // 6. 返回 token 与 username，响应格式与前端约定一致（R.code=0，字段平铺在 body）
        return R.ok().put("token", token).put("username", username);
    }
}
