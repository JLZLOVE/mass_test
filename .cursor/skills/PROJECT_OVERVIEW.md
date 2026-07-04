# Mass_Test 项目概览文档

> 本文档基于 `.cursor/skills/更新信息.md` 扫描结果维护，用于团队沟通与后续重构参考。  
> 更新时间：2026-07-04（用户状态 / 角色分配模块）  
> 文档位置：`.cursor/skills/PROJECT_OVERVIEW.md`

---

## 1. 项目概览

### 1.1 基本信息

| 项 | 内容 |
|---|---|
| **项目名称** | Mass_Test1（社团综合管理平台 / Club Administration System） |
| **Maven 坐标** | `com.unity:Mass_Test1:0.0.1-SNAPSHOT` |
| **项目描述** | springboot学习框架 |
| **仓库类型** | 前后端分离单体仓库（Spring Boot 后端 + Vue 3 管理端） |
| **主启动类** | `untiy.MassTestApplication` |
| **服务地址** | `http://localhost:100/Mass_Test` |
| **API 文档** | Knife4j：`http://localhost:100/Mass_Test/doc.html` |

### 1.2 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.2.0 |
| 安全 | Spring Security + JWT (jjwt) | 0.11.5 |
| 缓存 | Spring Data Redis | — |
| API 文档 | Knife4j / springdoc-openapi | 4.3.0 |
| 数据库 | MySQL | 8.0.33 |
| 连接池 | Druid | — |
| 前端 | Vue 3 + TypeScript + Vite | — |
| UI | Element Plus | — |
| 状态管理 | Pinia | — |
| 路由 | Vue Router 5（Hash 模式） | — |
| 图表 | ECharts | — |
| 工具 | Lombok、Fastjson、MapStruct | 1.2.83 |
| Java 版本 | JDK | 1.8 |

### 1.3 关键配置（`application.yml`）

| 配置项 | 值 |
|---|---|
| 服务端口 | `100` |
| Context Path | `/Mass_Test` |
| 数据库 URL | `jdbc:mysql://127.0.0.1:3306/mass_test1` |
| 数据库用户 | `root` / `123456` |
| Redis | `127.0.0.1:6379`，password `123456`，database `0` |
| **JWT 过期时间** | **`3600000` ms（1 小时）** ← 已由原 `360000` ms（6 分钟）延长 |
| JWT Secret | `jwt.secret`（见配置文件，生产环境需更换） |
| 文件上传限制 | 300MB |
| MyBatis-Plus | `table-underline: true`，`column-underline: true` |
| 日志级别 | `untiy.mapper: debug`，`untiy.filter: debug`，`org.springframework.security: DEBUG` |

### 1.4 根目录结构

```
Mass_Test/
├── pom.xml
├── PROJECT_OVERVIEW.md             # 根目录副本
├── src/                            # 后端源码
├── club-admin-frontend/            # Vue 3 管理端
├── mysql/                          # 数据库脚本
└── .cursor/skills/
    ├── PROJECT_OVERVIEW.md         # 本文档
    ├── 更新信息.md                 # 文档更新扫描清单
    ├── 项目.md                     # 项目分析指令
    ├── login.md                    # JWT/登录规范
    ├── jwt.md                      # JWT 安全重构说明
    ├── 权限过滤.md                 # 五级可见范围与权限过滤方案
    └── skill.md                    # 前端开发规范
```

---

## 2. 数据库设计

> 数据源：`mysql/mass_test1.sql`（MySQL 8.0.13，Schema：`mass_test1`）

### 2.1 表间关系

```
RBAC：sys_user ←→ sys_user_role ←→ sys_role → sys_role_menu ←→ sys_menu
组织架构：sys_college → sys_major；sys_club → sys_department
活动：activity_category → activity_apply → activity_approve_flow / activity_sign
通知：notice_category → notice_info → notice_read_record
统计：club_statistics（club_id + stat_date）
```

### 2.2 全表清单（18 张）

| 表名 | 说明 |
|---|---|
| `sys_user` | 用户（username=学号/工号） |
| `sys_role` | 角色（role_code、role_level、data_scope） |
| `sys_user_role` | 用户-角色（scope_type/scope_id 数据范围） |
| `sys_menu` / `sys_role_menu` | 菜单与角色菜单 |
| `sys_data_permission` | 数据权限规则 |
| `sys_college` / `sys_major` / `sys_club` / `sys_department` | 组织架构 |
| `activity_*` | 活动分类、申请、审批、签到 |
| `notice_*` | 通知分类、内容、阅读记录 |
| `club_statistics` | 社团日统计 |

### 2.3 实体时间字段序列化

所有含 `createTime` / `updateTime`（及其他 `time` 类字段）的实体类已统一添加：

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
```

涉及实体（代码已修改，本文档仅记录）：`SysUser`、`SysRole`、`SysUserRole`、`SysClub`、`SysCollege`、`SysDepartment`、`SysMajor`、`SysMenu`、`SysRoleMenu`、`SysDataPermission`、`ActivityApply`、`ActivityApproveFlow`、`ActivityCategory`、`ActivitySign`、`NoticeCategory`、`NoticeInfo`、`NoticeReadRecord`、`ClubStatistics` 等。

### 2.4 种子数据说明

- SQL 种子部分 `password` 为明文，应用使用 `BCryptPasswordEncoder` 校验。
- 建议通过 `POST /register/single` 注册，或执行 `mysql/fix_bcrypt_passwords.sql`。

---

## 3. 代码架构

### 3.1 后端包结构（`src/main/java/untiy`）

```
untiy/
├── controller/          (21)       # REST 接口层
├── service/ + impl/                # 业务逻辑
├── mapper/              (19)       # MyBatis-Plus 数据访问
├── entity/                         # 实体、DTO、VO
├── annotation/          (6)        # 自定义注解 + LevelAspect
├── config/              (9)        # Spring / JWT / Redis / CORS 等
├── security/            (10)       # SecurityConfig + 权限工具（含 LevelBasedAccess、UserSecurityHelper、UserRoleScopeHelper、UserCacheHelper）
├── filter/              (2)        # JwtFilter、IgnorePathsProperties
├── converter/           (2)        # MapStruct：SysUserConverter、SysRoleConvert
├── advice/                         # GlobalExceptionHandler
├── exception/                      # EIException、ErrorConfig、Level 常量
└── utils/                          # R、JwtUtil、MPUtil、SecurityUtils 等
```

### 3.2 权限三层分离（当前架构）

| 层级 | 职责 | 实现位置 |
|---|---|---|
| **准入鉴权** | 控制哪些等级可调用接口 | `@RequiresLevel` + `LevelAspect` |
| **数据行过滤** | 按等级拼接查询条件 | `DataScopeHelper`（用户模块）、`LevelBasedAccess`（通用等级过滤） |
| **字段脱敏** | 低权限隐藏敏感 DTO 字段 | `FieldMaskHelper` + `UserSecurityHelper.toMaskedDto` |

> 设计原则：同一业务仅保留单一 Controller 接口；`@RequiresLevel` 不做数据/字段差异化。  
> **已完成重构**：`SysUserController`、`SysRoleController`、`SysUserRoleController`  
> **待重构**：其余 Controller 仍保留 `_F` / `_B` 双接口模式。

### 3.3 权限等级常量（`untiy.exception.Level`）

| 常量 | 值 | 含义 |
|---|---|---|
| `SUPER_ADMIN` | 0 | 超级管理员，可见全部 |
| `ADMIN` | 1 | 管理员/导员，可见全部学生 |
| `CLUB_LEADER` | 2 | 社长，可见本社团 |
| `DEPT_LEADER` | 3 | 部长，可见本部门 |
| `STUDENT` | 4 | 普通学生，仅可见自己 |

`effectiveLevel` 由 `UserScopeResolver` 根据 `sys_role.role_code` 解析（取所有角色中最高权限，即数值最小）。

---

## 4. 自定义注解（`annotation` 包）

| 注解 / 类 | 作用域 | 属性 | 用途 |
|---|---|---|---|
| `@IgnoreAuth` | METHOD, TYPE | 无 | 标记接口免 Token；由 `IgnoreAuthRegistry` 启动扫描，`JwtFilter` 路径匹配放行 |
| `@RequiresLevel` | METHOD, TYPE | `minLevel`（默认 0） | **仅做接口准入**：`effectiveLevel ≤ minLevel` 才允许访问 |
| `@StartBeforeEnd` | TYPE | `startField`、`endField`、`message` | JSR-303 校验：开始时间必须早于结束时间 |
| `StartBeforeEndValidator` | — | — | `@StartBeforeEnd` 校验器实现 |
| `LevelAspect` | — | — | AOP 环绕 `@RequiresLevel`，反射读取方法/类级注解，不介入数据过滤 |
| `IgnoreAuthConstants` | — | `REQUEST_ATTR = "IGNORE_AUTH"` | Filter 写入免鉴权请求标记 |

---

## 5. 配置类（`config` 包）

| 类 | 关键配置 |
|---|---|
| `JwtConfig` | `@ConfigurationProperties(prefix="jwt")`：`secret`、`expiration`（毫秒） |
| `SecurityConfig` | 见 §7.1 |
| `RedisConfig` | `RedisTemplate<String,Object>`，Jackson2Json 序列化 |
| `MybatisPlusConfig` | `PaginationInterceptor` 分页插件 |
| `CorsConfig` | 跨域配置 |
| `OpenApiConfig` | Knife4j / springdoc OpenAPI |
| `ConverterConfig` | MapStruct 组件扫描 |
| `WebMvcConfig` | 注册 `IgnoreAuthInterceptor` |
| `IgnoreAuthRegistry` | 启动扫描 `@IgnoreAuth`，注册 HTTP 方法 + Ant 路径 |
| `IgnoreAuthInterceptor` | DispatcherServlet 阶段设置 `IGNORE_AUTH` 请求属性 |

---

## 6. 过滤器（`filter` 包）

| 类 | 执行顺序 | 职责 |
|---|---|---|
| `JwtFilter` | `SecurityConfig` 中置于 `UsernamePasswordAuthenticationFilter` **之前** | 静态白名单 → `@IgnoreAuth` 匹配 → Token 校验 → Redis/`loadUserByUsername` 加载 `LoginUserDetails` → 写入 SecurityContext |
| `IgnorePathsProperties` | — | 绑定 `security.ignore.urls`（Swagger/静态资源，不含业务路径） |

**JwtFilter 流程摘要：**

1. `IgnorePathsProperties` 白名单 → 直接放行  
2. `IgnoreAuthRegistry.matches(request)` → 设置 `IGNORE_AUTH`，注入占位 Authentication  
3. 读取 Header `Token` → `JwtUtil.validateToken()`  
4. Redis Key `user:{username}`：优先读 `LoginUserDetails.CacheSnapshot`；旧版 `Collection` 缓存则回源 DB 并升级  
5. `SecurityContext` principal = `LoginUserDetails`

---

## 7. 安全模块（`security` 包）

| 类 | 职责 |
|---|---|
| `SecurityConfig` | 无状态 Session；仅 Swagger/静态 `permitAll()`；`AuthenticationEntryPoint` 返回 401 JSON；注册 `JwtFilter` |
| `LoginUserDetails` | 继承 `LoginServiceImpl`；组合 `SysUser` + `effectiveLevel` + `primaryClubId` + `primaryDepartmentId`；含 Redis 可序列化 `CacheSnapshot` |
| `UserScopeResolver` | 从角色列表解析 effectiveLevel、主社团/部门 scope_id |
| `DataScopeHelper` | 用户模块行级过滤：`applySysUserScope(QueryWrapper/LambdaQueryWrapper)`、`currentUser()`、`applyLevelScope(BaseQuery)` |
| `FieldMaskHelper` | 用户 DTO 脱敏：`maskSysUserDto(dto, viewer)` |
| **`LevelBasedAccess`** | **全局通用等级访问控制**（数值越小权限越高）：`checkViewable`、`checkOperable`、`checkNewLevel`、`applyLevelFilter` |
| **`UserSecurityHelper`** | **用户模块专用**：`assertUsersInScope`、`toMaskedDto`、`findInScope`、`findInScopeByUserId`、`findActiveInScope`、`assertUserEnabled`、`assertBatchNoDisabled` |
| **`UserRoleScopeHelper`** | **角色分配 scope 校验**：按 `sys_role.data_scope` 校验 `scopeType/scopeId` 组合；`assertNoDuplicateAssignment` 防重复/互斥 |
| **`UserCacheHelper`** | **Redis 登录缓存清理**：`evictByUsernames(redisTemplate, usernames)`，Key 规则 `user:{username}` |
| `UserPermissionUtils` | 删除权限细粒度校验（不能删自己、等级比较、同社团 scope 等） |

### 7.0 工具类 `SecurityUtils`（`utils` 包）

| 方法 | 说明 |
|---|---|
| `getCurrentUser()` | 从 `SecurityContext` 取 `LoginUserDetails`；未登录抛 `EIException(LOGIN_INVALID_MSG)` |
| `getCurrentLevel()` | 返回当前用户 `effectiveLevel` |

> 被 `SysRoleServiceImpl`、`LevelBasedAccess` 等模块复用，与 `DataScopeHelper.currentUser()`（可返回 null）不同，`SecurityUtils` 在业务层强制要求已登录。

### 7.1 JWT 认证与 Redis 缓存

```
登录：POST /login/allocation → AuthenticationManager → UserDetailServiceImpl
     → JwtUtil.generateToken(username) → Redis 写入 user:{username}
请求：JwtFilter → 校验 Token → 加载 LoginUserDetails → SecurityContext
```

| Redis 项 | 规范 |
|---|---|
| Key | `user:{username}` |
| Value（新） | `LoginUserDetails.CacheSnapshot`（Jackson 序列化） |
| Value（旧，兼容） | `Collection<GrantedAuthority>`，命中后回源并升级 |
| TTL | 1 小时（Filter / LoginController 常量 `CACHE_TTL_HOURS = 1`） |
| JWT 过期 | `jwt.expiration = 3600000`（1 小时，与 Redis TTL 对齐） |

### 7.2 等级访问控制约定

**约定：数值越小权限越高**（与 `sys_role.role_level`、`Level` 常量一致）。

#### `LevelBasedAccess`（全局通用）

| 方法 | 用途 | 失败异常 |
|---|---|---|
| `checkViewable(current, target)` | 查看目标资源 | `ROLE_NO_PERMISSION` |
| `checkOperable(current, target)` | 修改/删除目标资源 | `ROLE_CANNOT_MODIFY_HIGHER` |
| `checkNewLevel(current, newLevel)` | 新增或变更等级字段 | `ROLE_CANNOT_ELEVATE` |
| `applyLevelFilter(wrapper, level, column)` | QueryWrapper 追加 `ge(column, level)` | — |
| `applyLevelFilter(BaseQuery, level)` | 注入数据范围字段（委托 `DataScopeHelper`） | — |

#### `UserSecurityHelper`（用户模块专用）

| 方法 | 用途 |
|---|---|
| `assertUsersInScope(mapper, sysUsers)` | 批量校验用户是否在数据范围内 |
| `toMaskedDto(converter, entity, viewer)` | Entity→DTO + `FieldMaskHelper` 脱敏 |
| `findInScope(mapper, username)` | 按 username 查用户并叠加数据范围 |
| `findInScopeByUserId(mapper, userId)` | 按 userId 查用户并叠加数据范围 |
| `findActiveInScope(mapper, userId)` | 在范围内且 `status=1`（供角色分配等） |
| `assertUserEnabled(user)` | 校验用户存在且未禁用 |
| `assertBatchNoDisabled(mapper, sysUsers)` | 批量更新前拒绝含已禁用用户的批次 |

#### `UserRoleScopeHelper`（角色分配 scope 校验）

按 `sys_role.data_scope`（非 `role_code` 硬编码）校验分配时的 `scopeType/scopeId`：

| `data_scope` | 含义 | 分配要求 | `scope_type` 对应 |
|---|---|---|---|
| 0 | 全部 | `scopeType`、`scopeId` 均为 null | — |
| 1 | 本学院 | `scopeType=1` 且学院 ID 存在 | 1=学院 |
| 2 | 本社团 | `scopeType=2` 且社团 ID 存在 | 2=社团 |
| 3 | 本部门 | `scopeType=3` 且部门 ID 存在 | 3=部门 |
| 4 | 仅自己 | `scopeType`、`scopeId` 均为 null | — |

`assertNoDuplicateAssignment`：精确匹配重复拒绝；全局与特定范围互斥。

#### `UserCacheHelper`（Redis 缓存）

| 方法 | 用途 |
|---|---|
| `evictByUsernames(redisTemplate, usernames)` | 批量删除 `user:{username}` 缓存（禁用/启用后强制重新加载） |

### 7.3 五级数据可见范围（用户模块 Service 层）

| effectiveLevel | 身份 | `applySysUserScope` 行为 |
|---|---|---|
| 0 | 超管 | 无额外条件 |
| 1 | 导员/管理员 | `user_type = 1`（仅学生） |
| 2 | 社长 | `id IN (sys_user_role WHERE scope_type=2 AND scope_id=clubId)` |
| 3 | 部长 | `id IN (sys_user_role WHERE scope_type=3 AND scope_id=deptId)` |
| 4 | 普通学生 | `id = 当前用户 ID` |

### 7.4 白名单（`security.ignore.urls`）

```
/v2/api-docs/**, /v3/api-docs/**, /swagger-resources/**, /swagger-ui/**,
/doc.html, /webjars/**, /favicon.ico, /js/**, /css/**, /img/**
```

> 业务接口（含 `/login/**`、`/register/**`）**不在** YAML 白名单，统一由 `@IgnoreAuth` + `JwtFilter` 控制。

---

## 8. 转换器（`converter` 包）

| 类 | 映射 | 说明 |
|---|---|---|
| `SysUserConverter`（MapStruct） | `SysUser` ↔ `SysUserDTO` | 脱敏由 `UserSecurityHelper.toMaskedDto` + `FieldMaskHelper` 完成 |
| `SysRoleConvert`（MapStruct） | `SysRole` → `SysRoleDTO`；`SysRoleDTO` → `SysRoleDTO`（`toEntity`） | 角色模块 DTO 转换，`componentModel = "spring"` |

---

## 9. 异常码（`ErrorConfig` 节选）

### 9.1 角色相关（5001~5007、400）

| Code | 常量 | 消息 |
|---|---|---|
| 5001 | `ROLE_NOT_FOUND` | 角色不存在 |
| 5002 | `ROLE_LEVEL_INSUFFICIENT` | 无权创建高于自身等级的角色 |
| 5003 | `ROLE_CANNOT_MODIFY_HIGHER` | 不能修改更高等级的角色 |
| 5004 | `ROLE_CANNOT_ELEVATE` | 不能提升自身权限等级 |
| 5005 | `ROLE_IN_USE` | 不能删除当前用户正在使用的角色 |
| 5006 | `ROLE_NO_PERMISSION` | 无权查看该角色 |
| 5007 | `ROLE_CODE_BLANK` | 角色编码不能为空 |
| 400 | `BAD_REQUEST` | 请求参数无效 |

### 9.2 用户状态 / 角色分配（5008~5014）

| Code | 常量 | 消息 |
|---|---|---|
| 5008 | `USER_DISABLED` | 用户已被禁用，无法操作 |
| 5009 | `CANNOT_DISABLE_SELF` | 不能禁用当前登录账号 |
| 5010 | `BATCH_CONTAINS_DISABLED` | 批量操作中包含已禁用用户，已拒绝 |
| 5011 | `ROLE_ASSIGN_DUPLICATE` | 角色分配冲突：已存在相同或互斥的范围记录 |
| 5012 | `ROLE_SCOPE_INVALID` | 角色数据范围不合法 |
| 5013 | `ROLE_REVOKE_SELF` | 不能撤销自己当前持有的角色 |
| 5014 | `USER_ROLE_NOT_FOUND` | 用户角色关联不存在 |

### 9.3 用户 / 登录相关（节选）

| Code | 常量 | 消息 |
|---|---|---|
| 001 | `LOGIN_INVALID` | 未登录或会话已失效 |
| 1008 | `USERNAME_BLANK` | 用户名为空 |
| 0002 | `NO_PERM_DELETE_USER` | 无权改变用户: |
| 3005 | `NOT_LOGGED_IN` | 未登录 |

---

## 10. 核心 Service

### 10.1 `UserDetailServiceImpl`

| 项 | 说明 |
|---|---|
| 实现 | `UserDetailsService` |
| 入口 | `loadUserByUsername(String username)` |
| 依赖 | `SysUserMapper`、`SysUserRoleMapper`、`SysRoleMapper`、`AuthorService` |
| 加载流程 | 查 `sys_user` → **`status≠1` 抛 `USER_DISABLED`** → 查 `sys_user_role` → 查有效 `sys_role` → `UserScopeResolver` 解析等级与 scope → 返回 `LoginUserDetails` |
| 特点 | **一次性加载**用户、角色、等级、社团/部门范围，避免 Filter 内二次查等级 |

### 10.2 `SysUserServiceImpl`

| 方法 | 行级过滤 | 字段脱敏 | 说明 |
|---|---|---|---|
| `pageQuery` | ✅ `DataScopeHelper` | ✅ `UserSecurityHelper.toMaskedDto` | 分页 + 条件 + 排序 |
| `getDetail(username)` | 按 username 查实体 | ✅ `UserSecurityHelper` | 用户不存在/用户名为空抛 `EIException` |
| `saveUser` | — | — | BCrypt 加密密码 |
| `updateUsers` | ✅ `assertUsersInScope` + **`assertBatchNoDisabled`** | — | 含已禁用用户整批拒绝 |
| `updateUser` | — | — | 学生仅可改自己；白名单字段（realName/gender/phone/email/avatar） |
| **`toggleStatus`** | ✅ `findInScope` | — | 批量启用/禁用；禁用时不可含自己；**`UserCacheHelper.evictByUsernames`** |
| **`listDisabled`** | ✅ `applySysUserScope` + `status=0` | ✅ | 关键词模糊搜索 username/realName |
| `deleteByUsername` / `deleteUsers` | ✅ `UserSecurityHelper.findInScope` | — | 越权抛 `AccessDeniedException` |
| `register` | — | — | 注册逻辑（`RegisterController` 调用） |

### 10.3 `SysRoleServiceImpl`

| 方法 | 等级控制 | 说明 |
|---|---|---|
| `pageQuery` | ✅ `LambdaQueryWrapper.ge(roleLevel, currentLevel)` | 仅用 `MPUtil.getPage`；搜索限 `roleName`/`roleCode` |
| `getDetail(id)` | ✅ `LevelBasedAccess.checkViewable` | `id==null` 抛 `BAD_REQUEST`；越权 `log.warn` |
| `saveRole` | ✅ `checkNewLevel` + `roleCode` 非空校验 | — |
| `updateRole` | ✅ `doUpdateRole`：`checkOperable` + `checkNewLevel` | `LambdaUpdateWrapper` 选择性更新，避免 null 覆盖 |
| `updateRoles` | ✅ 逐条 `doUpdateRole` | 外层 `@Transactional` 统一回滚 |
| `deleteById` / `deleteByIds` | ✅ `doDeleteById`：`checkOperable` + `checkRoleNotInUse` | 删除前同步清理 `sys_user_role`；批量 ID 去重 |
| `checkRoleNotInUse` | — | 权限格式与 `AuthorServiceImpl` 一致：`ROLE_` + roleCode |

**私有方法：** `doUpdateRole`、`doDeleteById`、`removeUserRoleAssociations`、`buildRoleAuthority`

### 10.4 `SysUserRoleServiceImpl`

| 方法 | 等级/范围控制 | 说明 |
|---|---|---|
| `assign(AssignRoleDTO)` | ✅ `findActiveInScope` + `LevelBasedAccess.checkNewLevel` | 目标须未禁用且在数据范围；**`UserRoleScopeHelper.validateScope(role.dataScope, ...)`**；防重复 |
| `revoke(id)` | ✅ `checkOperable` + `assertNotRevokingOwnRole` | 不可撤销高于自身等级；不可撤销自己持有的角色 |
| `pageQuery(param, keyword)` | ✅ `DataScopeHelper.applySysUserScope` → 限定 userIds | 联查 `SysUserRoleVO`（username、realName、roleName） |
| `listMyRoles()` | — | 当前登录用户角色列表 |

---

## 10A. 实体 / DTO / VO / Mapper（用户-角色模块）

### DTO

| 类 | 路径 | 字段 | 用途 |
|---|---|---|---|
| `AssignRoleDTO` | `entity/dto/AssignRoleDTO.java` | `userId`、`roleId`、`scopeType`、`scopeId` | `POST /assign` 请求体；`scopeType/scopeId` 须符合目标角色 `data_scope` |
| `ToggleStatusDTO` | `entity/dto/ToggleStatusDTO.java` | `usernames`、`status`（0=禁用，1=启用） | `PUT /toggleStatus` 请求体 |

### VO

| 类 | 路径 | 字段 |
|---|---|---|
| `SysUserRoleVO` | `entity/vo/SysUserRoleVO.java` | `id`、`userId`、`username`、`realName`、`roleId`、`roleName`、`roleCode`、`scopeType`、`scopeId`、`createTime` |

### Mapper

| 接口 / XML | 方法 | 说明 |
|---|---|---|
| `SysUserRoleMapper.selectPageWithDetail` | 分页联查 | `sys_user_role` JOIN `sys_user` JOIN `sys_role`；按 `userIds` 范围 + 关键词（username/realName/roleName） |
| `SysUserRoleMapper.selectListByUserId` | 列表 | 按 `userId` 查当前用户全部角色关联 |

---

## 11. 接口清单（重点 Controller）

> 完整路径前缀：`/Mass_Test`  
> 统一响应：`R { code, msg, data? }`  
> 认证 Header：`Token: <jwt>`

### 11.1 认证模块

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/login/allocation` | POST | `@IgnoreAuth` | `name` + `password` → `{ token, username }` |
| `/register/single` | POST | `@IgnoreAuth` | `RegisterDTO` 注册 |

### 11.2 `SysUserController`（已统一单接口）

根路径：`/sys-user`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/listSysUser` | GET | `minLevel = STUDENT (4)` | 分页查询；Service 自动行过滤 + DTO 脱敏 |
| `/detailSysUser/{username}` | GET | `STUDENT (4)` | 按 username 查详情 |
| `/addSysUser` | POST | `ADMIN (1)` | 新增用户 |
| **`/toggleStatus`** | PUT | `ADMIN (1)` | 批量启用/禁用；Body: `ToggleStatusDTO`；更新后清 Redis |
| **`/listDisabled`** | GET | `ADMIN (1)` | 分页查已禁用用户（`status=0`）；支持 `keyword` |
| `/updateSysUser` | PUT | `STUDENT (4)` | 更新**自己**的信息（白名单字段） |
| `/updateSysUserBatc` | PUT | `ADMIN (1)` | 批量更新；含已禁用用户整批拒绝 |
| `/deleteSysUser/{username}` | DELETE | `ADMIN (1)` | 单个删除 |
| `/deleteSysUser` | DELETE | `ADMIN (1)` | 批量删除，Body: `List<String>` usernames |

**已移除的旧接口：** `listSysUser_F/B`、`detailSysUser_F/B`、`add_B`、`updateSysUser_B`、`deleteSysUser_B`、`query`（公开）

### 11.3 `SysUserRoleController`（已统一为 4 接口）

根路径：`/sys-user-role`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| **`/assign`** | POST | `ADMIN (1)` | 分配角色；Body: `AssignRoleDTO`；校验 `data_scope`、防重复、目标未禁用 |
| **`/revoke/{id}`** | DELETE | `ADMIN (1)` | 撤销角色关联；不可撤销高于自身等级或自己持有的角色 |
| **`/list`** | GET | `ADMIN (1)` | 分页联查；支持 `keyword`；按管理员数据范围过滤 |
| **`/my-roles`** | GET | `STUDENT (4)` | 当前登录用户角色列表 |

**已移除的旧接口：** `listSysUserRole`、`listSysUserRole_F/B`、`query`、`detailSysUserRole_F/B`、`add_B`、`updateSysUserRole_B`、`deleteSysUserRole_B`、`/revoke`（无路径参数版）

### 11.4 `SysRoleController`（已统一单接口）

根路径：`/sys-role`（对标 `SysUserController` 设计）

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/listSysRole` | GET | `STUDENT (4)` | 分页查询；Service 按 `role_level >= 当前等级` 过滤 |
| `/detailSysRole/{id}` | GET | `STUDENT (4)` | 详情；不可查看高于自身等级的角色 |
| `/addSysRole` | POST | `ADMIN (1)` | 新增；`roleCode` 非空；不可创建高于自身等级 |
| `/updateSysRole` | PUT | `ADMIN (1)` | 单个更新；`LambdaUpdateWrapper` 选择性更新 |
| `/updateSysRoleBatc` | PUT | `ADMIN (1)` | 批量更新（路径名为 Batc） |
| `/deleteSysRole/{id}` | DELETE | `ADMIN (1)` | 单个删除；同步清理 `sys_user_role` |
| `/deleteSysRole` | DELETE | `ADMIN (1)` | 批量删除，Body: `List<Long>` ids |

**已移除的旧接口：** `listSysRole_F/B`、`detailSysRole_F/B`、`add_B`、`updateSysRole_B`、`deleteSysRole_B`、`query`

### 11.5 其他 Controller 根路径

| Controller | 根路径 |
|---|---|
| `SysRoleMenuController` | `/sys-role-menu` |
| `SysMenuController` | `/sys-menu` |
| `SysDataPermissionController` | `/sys-data-permission` |
| `SysCollegeController` | `/sys-college` |
| `SysDepartmentController` | `/sys-department` |
| `SysMajorController` | `/sys-major` |
| `SysClubController` | `/sys-club` |
| `ActivityCategoryController` | `/activity-category` |
| `ActivityApplyController` | `/activity-apply` |
| `ActivityApproveFlowController` | `/activity-approve-flow` |
| `ActivitySignController` | `/activity-sign` |
| `NoticeCategoryController` | `/notice-category` |
| `NoticeInfoController` | `/notice-info` |
| `NoticeReadRecordController` | `/notice-read-record` |
| `ClubStatisticsController` | `/club-statistics` |

---

## 12. 前端结构（`club-admin-frontend`）

```
src/api/crudFactory.ts    # 仍生成 listF/listB 等（待对齐 SysUser/SysRole 新接口）
src/utils/request.ts      # baseURL=/Mass_Test，Header Token 注入
src/stores/user.ts        # token、username、角色
src/router/dynamic.ts     # 后端菜单驱动动态路由
```

**开发服务器：** 端口 `5173`，代理 `/Mass_Test` → `http://localhost:100`

---

## 13. 关键业务流程

### 13.1 登录 → 权限缓存 → 业务请求

```
login → JWT(subject=username) → Redis user:{username}
     → 业务请求携带 Token → JwtFilter 加载 LoginUserDetails
     → @RequiresLevel 准入 → Service 数据过滤（DataScopeHelper / LevelBasedAccess）+ 字段脱敏（UserSecurityHelper）
```

### 13.3 用户状态与角色分配

```
禁用用户：PUT /toggleStatus → findInScope → 不可禁自己 → 更新 status → UserCacheHelper 清 Redis
登录拦截：UserDetailServiceImpl → status≠1 → USER_DISABLED(5008)
批量更新：updateUsers → assertBatchNoDisabled → 含禁用用户整批拒绝
角色分配：POST /assign → findActiveInScope → validateScope(role.dataScope) → assertNoDuplicateAssignment → 写入 sys_user_role
角色撤销：DELETE /revoke/{id} → checkOperable → assertNotRevokingOwnRole → 删除关联
```

### 13.2 活动 / 通知（概要）

- 活动：`activity_apply` 审批状态 1~6；`activity_approve_flow` 多步审批；`activity_sign` 签到  
- 通知：`notice_info.receiver_type` + `receiver_values` → `notice_read_record`

---

## 14. 模块依赖

```mermaid
flowchart TB
    subgraph Frontend["club-admin-frontend"]
        V[views] --> A[api]
        A -->|HTTP /Mass_Test| C
    end

    subgraph Backend["Spring Boot"]
        C[Controller] -->|@RequiresLevel| LA[LevelAspect]
        C --> SV[Service]
        SV --> DS[DataScopeHelper]
        SV --> LBA[LevelBasedAccess]
        SV --> USH[UserSecurityHelper]
        SV --> URS[UserRoleScopeHelper]
        SV --> UCH[UserCacheHelper]
        SV --> FM[FieldMaskHelper]
        SV --> SU[SecurityUtils]
        SV --> M[Mapper]
        M --> DB[(MySQL)]
        JF[JwtFilter] --> SEC[SecurityConfig]
        JF --> RD[(Redis)]
        UDS[UserDetailServiceImpl] --> LUD[LoginUserDetails]
    end
```

---

## 15. 已知问题与后续工作

| 状态 | 问题 | 说明 |
|---|---|---|
| ✅ 已修复 | JWT 过期过短 | 已改为 1 小时（`3600000` ms） |
| ✅ 已修复 | 401 vs 403 | `AuthenticationEntryPoint` 统一 401；`AccessDeniedException` → 403 |
| ✅ 已修复 | `@IgnoreAuth` Filter 阶段失效 | `IgnoreAuthRegistry` 启动扫描替代 `getHandler()` |
| ✅ 已完成 | 用户模块单接口化 | `SysUserController` + `SysUserServiceImpl` + `UserSecurityHelper` |
| ✅ 已完成 | 角色模块单接口化 | `SysRoleController` + `SysRoleServiceImpl` + `LevelBasedAccess` |
| ✅ 已完成 | 角色 CRUD 安全加固 | 选择性更新、级联删 `sys_user_role`、越权 `log.warn`、ID 去重 |
| ✅ 已完成 | 用户状态管理 | `toggleStatus`、`listDisabled`；登录/批量更新/分配全局拦截禁用用户 |
| ✅ 已完成 | 用户-角色模块重构 | `SysUserRoleController` 4 接口；`UserRoleScopeHelper` 按 `data_scope` 校验 |
| ✅ 已修复 | `data_scope` 分配无效 | `validateScope` 改为读取 `role.getDataScope()`，不再硬编码 `roleCode` |
| ⏳ 待做 | 其余 Controller `_F/_B` 合并 | 按用户/角色模块模式逐步重构 |
| ⏳ 待做 | 前端 `crudFactory` 对齐 | `listF/listB` → 统一 `listSysUser` / `listSysRole` 等 |
| ⏳ 待做 | 种子密码明文 | BCrypt 迁移或重新注册 |
| ⏳ 待做 | entity / model 重复 | 统一实体包 |
| ⏳ 待做 | Fastjson 1.2.83 安全风险 | 升级或替换 Jackson |

---

## 16. 相关文档

| 文档 | 路径 | 说明 |
|---|---|---|
| **项目概览（本文档）** | `.cursor/skills/PROJECT_OVERVIEW.md` | 结构与接口说明 |
| 更新扫描清单 | `.cursor/skills/更新信息.md` | 本文档更新依据 |
| JWT 登录规范 | `.cursor/skills/login.md` | Redis Key、subject 规范 |
| 权限过滤方案 | `.cursor/skills/权限过滤.md` | 五级可见范围 |
| 前端开发规范 | `.cursor/skills/skill.md` | Vue3/Element Plus |
| OpenAPI JSON | `src/api/spec/api-docs.json` | 机器可读 API |
| 数据库脚本 | `mysql/mass_test1.sql` | 建表 + 种子 |

---

*文档结束*
