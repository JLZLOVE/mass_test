package untiy.controller;

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
import untiy.service.impl.UserDetailServiceImpl;
import untiy.utils.JwtUtil;
import untiy.utils.R;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/allocation")
    public R login(@RequestParam String name, @RequestParam String password) {
        // 1. 创建认证令牌
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(name, password);

        // 2. 进行认证（Spring Security 会自动调用 UserDetailServiceImpl.loadUserByUsername 并比对密码）
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. 认证成功后，获取 UserDetails（即LoginServiceImpl）
        LoginServiceImpl userDetails = (LoginServiceImpl) authentication.getPrincipal();

        // 4. 生成 JWT（只包含用户 ID）
        String token = jwtTokenUtil.generateToken(userDetails.getId().toString());

        // 5. 将用户权限缓存到 Redis
        redisTemplate.opsForValue().set("user:" + userDetails.getId(),
                userDetails.getAuthorities(),
                1, TimeUnit.HOURS); // 过期时间设为 1 小时

        // 6. 返回 token 给前端（可以同时返回一些非敏感信息，如用户名）
        return R.ok().put("token", token).put("username", userDetails.getUsername());
    }
}
