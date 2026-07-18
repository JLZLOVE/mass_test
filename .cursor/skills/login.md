# JWT 过滤器与登录 / 注销规范（与代码同步）

> 更新：2026-07-18。权威细节以 `PROJECT_OVERVIEW.md` §7.1 / §11.1 为准。  
> 历史「问题背景」章节保留作演进说明；**硬性规则以本节现行约定为准**。

---

## 1. 现行认证链路（必须）

```
登录 POST /login/allocation
  → AuthenticationManager → UserDetailServiceImpl
  → JwtUtil.generateToken(username)   // subject = username，禁止存 DB id
  → Redis SET user:v2:{username} = CacheSnapshot，TTL 1h

请求 JwtFilter
  → 校验 Token → 解析 username
  → 读取 Redis user:v2:{username}
  → 命中：注入 LoginUserDetails → SecurityContext
  → 缺失/脏数据：直接 401（不回源 DB）

注销 POST /login/logout（需登录，RequiresLevel STUDENT）
  → UserCacheHelper.evictByUsername
  → 删 user:v2:{username}（及旧 user:{username}）
  → 返回 R.ok("已退出")；不手动 clearContext
```

---

## 2. Redis 规范（现行）

| 项 | 约定 |
|---|---|
| Key | **`user:v2:{username}`**（`UserCacheHelper.keyForUsername`） |
| Value | `LoginUserDetails.CacheSnapshot`（字段 + 构造参数均 `@JsonProperty`） |
| 旧 Key | `user:{username}` 仅在 evict 时删除 |
| 未命中 | **401**，禁止 `loadUserByUsername` 补写（否则注销失效） |
| TTL | 与 JWT 对齐：1 小时 |

---

## 3. 接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/login/allocation` | POST | `@IgnoreAuth` | `name`+`password` → `{token, username}` |
| `/login/logout` | POST | `STUDENT` | 删 Redis 会话；幂等 |

Header：`Token` 或 `Authorization: Bearer ...`

---

## 4. 硬性规则

### 4.1 JWT
- subject **仅** `username`（学号/工号）
- 使用 `getUsernameFromToken`，勿用易混淆的 `getUserIdFromToken`

### 4.2 过滤器
- 白名单：`security.ignore.urls` + `@IgnoreAuth` 注册表
- 认证失败统一 HTTP **401** JSON：`{"code":401,"msg":"未登录或Token无效"}`
- principal 为 `LoginUserDetails`，与业务 `SecurityUtils` 一致

### 4.3 注销
- 只删缓存，不引入 Token 黑名单（校园场景可接受「重新登录后旧 Token 复活」）
- 与管理员 `toggleStatus` → `evictByUsernames` 同一套 Key

---

## 5. 历史冲突（已修复，勿再引入）

| 错误模式 | 现行 |
|---|---|
| subject 存 DB id | 存 username |
| Key `user:` / `user:details:` 混用 | 统一 `user:v2:` |
| 缓存 `Collection<GrantedAuthority>` | 缓存 `CacheSnapshot` |
| 缓存未命中回源 DB | **禁止**，直接 401 |

---

## 6. 验证清单

- [ ] Token `sub` 为学号字符串  
- [ ] Redis 存在 `user:v2:学号`  
- [ ] logout 后同 Token 访问业务接口 → 401  
- [ ] 重新登录后可正常访问  
- [ ] 白名单与 `/portal/notice|activity|club/**` 静态图免登录可访问  
