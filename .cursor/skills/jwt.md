```
# Security 配置注册过滤器（@IgnoreAuth 鉴权改造）

**目标**：注册 JWT 过滤器，移除 Security 业务白名单，统一由 `@IgnoreAuth` 控制免登录。

**改动范围**：`SecurityConfig.java`、`application.yml`、`JwtFilter`。

**现行补充（2026-07-18）**：

- Redis 会话 Key：`user:v2:{username}`；缓存缺失 → 401（不回源）
- 注销：`POST /login/logout`
- 静态放行：`/portal/notice/**`、`/portal/activity/**`、`/portal/club/**`
- 代码仍可能使用 `WebSecurityConfigurerAdapter`（Boot 2.7）；重构目标可用 `SecurityFilterChain`

---

## 1. 修改 `SecurityConfig.java`

位置：`src/main/java/untiy/security/SecurityConfig.java`

使用 `SecurityFilterChain` Bean（Spring Boot 2.7+）：
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/doc.html", "/webjars/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```



- 移除所有业务路径白名单（如 `/login/**`）。
- 仅放行静态资源和 Swagger。

------

## 2. 清理 `application.yml`

删除 `security.ignore.urls` 中的业务路径，只保留文档和静态资源：

yaml

```
security:
  ignore:
    urls: /v3/api-docs/**, /swagger-ui/**, /doc.html, /webjars/**, /favicon.ico
```



（若 JwtFilter 依赖此配置，同步调整）

------

## 3. 确认 `JwtFilter` 注解检查逻辑

位置：`src/main/java/untiy/filter/JwtFilter.java`

确保注入 `RequestMappingHandlerMapping`，并在 `doFilterInternal` 中检查 `@IgnoreAuth`：

java

```
@Autowired private RequestMappingHandlerMapping handlerMapping;

// 在 doFilterInternal 中：
try {
    HandlerMethod handler = (HandlerMethod) handlerMapping.getHandler(request).getHandler();
    if (handler.getMethodAnnotation(IgnoreAuth.class) != null ||
        handler.getBeanType().getAnnotation(IgnoreAuth.class) != null) {
        chain.doFilter(request, response);
        return;
    }
} catch (Exception ignored) {}

// 无注解则校验 Token，失败返回 401 JSON
String token = request.getHeader("Token");
if (token == null || !jwtUtil.validateToken(token)) {
    response.setStatus(401);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token无效\"}");
    return;
}
chain.doFilter(request, response);
```



------

## 验证

| 场景                  | 操作             | 预期          |
| :-------------------- | :--------------- | :------------ |
| 有 `@IgnoreAuth` 接口 | 不带 Token 请求  | 200 成功      |
| 无注解接口            | 不带 Token       | 401 JSON 错误 |
| 无注解 + 有效 Token   | 正常请求         | 业务数据      |
| 静态资源/Swagger      | 访问 `/doc.html` | 正常显示      |

------

## 注意事项

- Token 失效响应必须设置 `Content-Type: application/json`。
- 若 `handlerMapping.getHandler()` 异常（如 404），应继续执行 Token 校验（已处理）。
- 跨域配置保留。
- 若仍继承 `WebSecurityConfigurerAdapter`，将上述内容改为重写 `configure(HttpSecurity)`。

------

## 常见问题

- **加 `@IgnoreAuth` 仍要求 Token**：检查 `handlerMapping.getHandler()` 是否获取到 `HandlerMethod`（路径是否存在）。
- **返回 403 而非 401**：可能被 `AuthenticationEntryPoint` 覆盖，检查 Security 配置。
- **静态资源被拦截**：补充 `antMatchers` 路径。