---
name: jwt-filter-login-fix
description: 社团管理平台后端 JWT 认证过滤器与登录接口逻辑统一修复规范。解决 Token 存储内容、Redis 缓存 Key、UserDetailsService 查询字段不一致导致的认证失败问题。本技能规定代码必须遵守的硬性规则，禁止出现特定错误模式。
---

# JWT 过滤器与登录接口逻辑统一规范（Cursor 专用）

## 1. 问题背景与根本原因

项目使用 Spring Boot 2.7.18 + Spring Security + JWT + Redis 实现无状态认证。登录接口 `/login/allocation`（GET，参数 `name` 和 `password`）认证成功后返回 JWT Token，并将用户权限缓存到 Redis。后续请求由 `JwtFilter` 拦截，从 Header `Token` 中提取 JWT，解析用户标识并从 Redis 或数据库加载权限，构建 `SecurityContext`。

**根本原因**：登录时错误地将数据库主键 ID（如 `41`）存入 JWT 的 subject 和 Redis Key，而业务层期望使用学号（username）作为唯一标识，导致查询失败和权限加载异常。最终前端收到 403 或 500，无法正常访问受保护接口。

---

## 2. 当前存在的冲突（必须修复）

| 组件 | 实际行为 | 预期行为 | 冲突影响 |
|------|----------|----------|----------|
| `LoginController` | 生成 JWT 时存入 `userDetails.getId()`（数据库主键 ID） | 存入 `userDetails.getUsername()`（学号/工号） | Token 内容为 ID，无法用于查询 username 字段 |
| `LoginController` 缓存 Key | `"user:" + userDetails.getId()` | `"user:" + userDetails.getUsername()` | 与后续过滤器读取的 Key 不一致 |
| `JwtFilter` 解析 Token | 调用 `getUserIdFromToken()` 得到 ID 字符串 | 应得到 username 字符串 | 变量名误导，实际返回的是 subject（应为 username） |
| `JwtFilter` 缓存读取 Key | `"user:details:" + userId` | `"user:" + username` | Key 与登录时写入的不一致，且前缀不同 |
| `JwtFilter` 缓存内容期望 | 期望 `UserDetails` 对象 | 期望 `Collection<GrantedAuthority>`（权限集合） | 登录时缓存的是权限集合，类型不匹配 |
| `JwtFilter` 数据库加载调用 | `loadUserByUsername(userDetails.getUsername())`（`userDetails` 为 null） | 应直接调用 `loadUserByUsername(username)` | 空指针异常 |
| `loadUserByUsername` 参数 | 传入 ID 字符串（如 `"41"`） | 传入 username（学号） | 数据库 `username` 列存储学号，查询失败 |
| 异常处理 | 未捕获 `UsernameNotFoundException` 或返回 500 | 应统一返回 401 | 用户体验差，泄露内部信息 |

---

## 3. 预期目标（必须达成）

- 用户登录成功后，携带 Token 访问任意受保护接口，能正确认证并返回 200 OK，而不是 403 或 500。
- 权限数据来源可靠：Redis 缓存优先，数据库回源，且缓存 Key 统一。
- 代码无空指针异常，日志清晰，异常响应统一为 401（认证失败）。
- 所有修改不破坏现有业务逻辑，兼容现有数据库表结构。
- 前端无需修改，仅后端调整即可恢复功能。

---

## 4. 必须修改的文件清单（优先级：高）

| 文件 | 修改必要性 | 修改要点 |
|------|------------|----------|
| `LoginController.java` | **必须** | 1. 生成 Token 时使用 `userDetails.getUsername()` 作为 subject。<br>2. 缓存 Key 改为 `"user:" + userDetails.getUsername()`。<br>3. 确保缓存内容为 `authorities` 集合。 |
| `JwtUtil.java` | **必须** | 1. 新增或重命名方法 `getUsernameFromToken(String token)`，明确返回 subject（即 username）。<br>2. 删除或弃用 `getUserIdFromToken` 方法以避免混淆（推荐）。 |
| `JwtFilter.java` | **必须** | 1. 从 Token 解析得到 username 后，直接用于 `loadUserByUsername(username)`。<br>2. 缓存 Key 改为 `"user:" + username`，读取权限集合而非 UserDetails。<br>3. 捕获 `UsernameNotFoundException` 并返回 401。<br>4. 避免在 `userDetails` 为 null 时调用其方法。 |
| `UserDetailServiceImpl.java` | **建议检查** | 确保 `loadUserByUsername` 方法能根据 username（学号）正确查询用户，并返回包含权限的 `UserDetails`。 |
| `SecurityConfig.java` | **无需修改** | 路径放行已正确配置，但需确认 `addFilterBefore` 的位置正确。 |

---

## 5. 硬性规则（代码必须遵守）

### 5.1 JWT 内容规范
- **必须**：JWT 的 subject（或自定义 claim）中**仅存储** `username`（学号/工号），**禁止**存储数据库主键 ID、密码或其他敏感信息。
- **必须**：`JwtUtil` 中提供语义清晰的方法，如 `getUsernameFromToken(String token)` 返回 subject，**不应**命名为 `getUserIdFromToken` 造成歧义。
- **必须**：Token 生成时，`generateToken` 方法的参数应显式命名为 `username` 或 `subject`，确保调用者明确其含义。

### 5.2 Redis 缓存规范
- **必须**：缓存 Key 统一为 `"user:" + username`（例如 `"user:202320164602"`）。
- **必须**：缓存内容统一为 `Collection<GrantedAuthority>`（权限集合），**禁止**缓存整个 `UserDetails` 对象（避免缓存密码等敏感数据，减少内存占用）。
- **必须**：登录成功后，立即写入权限缓存，过期时间设为 1 小时（可配置）。
- **必须**：在 `JwtFilter` 中，先从 Redis 读取权限，若未命中则从数据库加载，并**补写**缓存（复用同一 Key）。
- **必须**：禁止在登录时和过滤器中分别使用不同的 Key 前缀或命名（如 `"user:details:"` 与 `"user:"` 混用）。

### 5.3 过滤器逻辑规范
- **必须**：从 Token 解析出的用户名直接作为 `loadUserByUsername(String username)` 的参数，**禁止**传入其他字段或进行类型转换。
- **必须**：在调用 `loadUserByUsername` 之前，确保用户名不为空且非空字符串。
- **必须**：捕获 `UsernameNotFoundException`，并调用 `response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "认证失败")` 并 `return`，**禁止**将异常继续抛出或返回 500。
- **必须**：捕获其他异常（如 `NullPointerException`）时，记录错误日志并返回 500，但不应泄露堆栈信息给客户端。
- **必须**：构建 `UsernamePasswordAuthenticationToken` 时，`principal` 使用 `username` 字符串（或 `UserDetails` 对象，但需保持与业务层一致），`credentials` 为 `null`，`authorities` 为权限集合。
- **必须**：在 `SecurityContextHolder` 中设置认证信息后，继续执行 `chain.doFilter(request, response)`。

### 5.4 路径放行规范
- **必须**：使用 `@ConfigurationProperties(prefix = "security.ignore")` 注入白名单路径列表，**禁止**硬编码路径在 `JwtFilter` 中。
- **必须**：使用 `AntPathMatcher` 匹配路径，支持通配符（如 `/allocation/**`）。
- **必须**：在 `SecurityConfig` 的 `HttpSecurity` 中同时放行相同路径，确保这些路径不经过 `JwtFilter` 的认证拦截。
- **必须**：注意 `context-path`（如 `/Mass_Test`），使用 `request.getServletPath()` 获取相对路径，确保匹配准确。

### 5.5 日志规范
- **必须**：使用 SLF4J（`LoggerFactory.getLogger(...)`）记录日志，**禁止**使用 `System.out.println`。
- **必须**：在关键节点记录 debug 或 info 日志：路径拦截、Token 校验结果、缓存命中/未命中、用户加载成功/失败。
- **必须**：记录异常时包含足够的上下文（如 username），但避免记录密码或 Token 原文。

### 5.6 异常响应规范
- **必须**：所有认证失败场景（Token 无效、用户不存在、缓存加载失败）均返回 HTTP 状态码 401，**禁止**返回 403（403 通常用于权限不足，而非认证失败）。
- **必须**：响应内容仅包含简单错误信息（如 "认证失败"），**禁止**返回数据库错误、堆栈信息等敏感内容。

---

## 6. 禁止出现的错误模式（红灯清单）

- ❌ 在 `JwtFilter` 中调用 `userDetails.getUsername()` 而 `userDetails` 可能为 `null`。
- ❌ 在 Redis 缓存中混合存储不同类型（`UserDetails` 与 `GrantedAuthority` 集合混用）。
- ❌ 在登录时和过滤器中分别使用不同的 Key 前缀或命名。
- ❌ 在过滤器中使用 `userId` 变量名表示 username，应使用 `username` 避免混淆。
- ❌ 未配置白名单就直接放行所有路径，或白名单未生效导致登录接口也被拦截。
- ❌ 在异常处理中返回 500 或堆栈信息给客户端。
- ❌ 在生成 Token 时使用 `userDetails.getId()` 或其他非 username 字段。
- ❌ 修改 `UserDetailServiceImpl` 的查询字段（应保持查询 `username` 列）。
- ❌ 忽略 `context-path`，导致路径匹配失败（必须使用 `getServletPath()`）。

---

## 7. 修改步骤建议（按顺序执行）

1. **修改 `LoginController`**：调整 Token 生成和缓存 Key。
2. **修改 `JwtUtil`**：明确方法语义，确保 `getUsernameFromToken` 正确返回 subject。
3. **重构 `JwtFilter`**：按照规则重写 `doFilterInternal`，重点关注缓存 Key、异常处理和参数传递。
4. **清理 Redis 缓存**：删除所有旧格式的缓存 Key（`user:details:*` 和 `user:ID` 等），确保新格式生效。
5. **全面测试**：执行下方验证标准中的所有测试用例。
6. **部署验证**：观察日志，确认无空指针异常，且所有受保护接口正常返回。

---

## 8. 验证标准（必须通过测试）

- [ ] 登录接口返回的 Token 解码后，`sub` 字段为学号字符串（非数字 ID）。
- [ ] 登录后 Redis 中存在 `user:学号` 的 Key，且值类型为 `Collection<GrantedAuthority>`。
- [ ] 携带有效 Token 访问 `/sys-user/query?username=学号`，返回 200 且数据正确。
- [ ] 携带无效或过期 Token 访问受保护接口，返回 401。
- [ ] 访问白名单路径（如 `/login/**`、`/doc.html`）不经过 `JwtFilter`，直接放行。
- [ ] 日志中无空指针异常或 `UsernameNotFoundException` 被误报为 500。
- [ ] 过滤器执行后，`SecurityContextHolder.getContext().getAuthentication()` 不为空，且 `getPrincipal()` 返回 username。
- [ ] 权限变更后，清除缓存能立即生效（手动测试）。

---

## 9. 注意事项与灵活性说明

- 本规范为硬性要求，但具体代码实现（如方法名、变量声明等）允许根据项目现有代码风格酌情调整，前提是必须满足所有规则。
- 若 `JwtUtil` 中存在其他方法依赖，修改时需保持兼容，避免破坏其他模块。
- 如果项目中存在多个地方调用 `getUserIdFromToken`，应全局搜索并替换为 `getUsernameFromToken`，确保语义一致。
- 建议在修改前备份原始代码，并逐步提交，方便回滚。
- 若遇到数据库表 `username` 字段类型与长度问题，确保可以存储学号（通常为 varchar）。
- 前端代码无需修改，因为 Token 的 Header 名称和响应格式不变。

---

## 10. 最终目标

经过本次修改后，后端认证流程将完全打通，用户登录后携带 Token 访问所有受保护接口均能正常通过，前端不再收到 403 或 500 错误。系统运行稳定，日志清晰，异常处理健壮。

---

