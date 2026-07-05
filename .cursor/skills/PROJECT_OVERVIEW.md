# Mass_Test 项目概览文档

> 本文档基于 `.cursor/skills/更新信息.md` 扫描结果维护，用于团队沟通与后续重构参考。  
> 更新时间：2026-07-05（活动审批模块 + 社团申请/合议 + RBAC 模块）  
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
| **活动附件目录** | `activity.upload-dir: static/activity`（本地存储，预留 OSS） |
| **定时任务** | `@EnableScheduling`；活动审批超时扫描每小时一次 |

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
    ├── 活动.md                     # 社团创建/解散/合议业务流程
    ├── 活动审批模块.md             # 活动申请/审批/变更/取消/总结/超时
    └── skill.md                    # 前端开发规范
```

---

## 2. 数据库设计

> 数据源：`mysql/mass_test1.sql`（MySQL 8.0.13，Schema：`mass_test1`）

### 2.1 表间关系

```
RBAC：sys_user ←→ sys_user_role ←→ sys_role → sys_role_menu ←→ sys_menu
组织架构：sys_college → sys_major；sys_club → sys_department
社团生命周期：club_application（创建/解散申请）→ 学院/校级审批 → 激活社团；club_council（合议解散）
活动：activity_category → activity_apply → activity_approve_flow / activity_sign / activity_apply_history
通知：notice_category → notice_info → notice_read_record
统计：club_statistics（club_id + stat_date）
```

### 2.2 全表清单（21+ 张）

| 表名 | 说明 |
|---|---|
| `sys_user` | 用户（username=学号/工号） |
| `sys_role` | 角色（role_code、role_level、data_scope） |
| `sys_user_role` | 用户-角色（scope_type/scope_id 数据范围） |
| `sys_menu` / `sys_role_menu` | 菜单与角色菜单 |
| `sys_data_permission` | 数据权限规则 |
| `sys_college` / `sys_major` / `sys_club` / `sys_department` | 组织架构 |
| **`club_application`** | **社团创建/解散申请（学院+校级审批）** |
| **`club_council`** | **合议解散（JSON 签名人列表）** |
| `activity_category` | 活动分类 |
| **`activity_apply`** | **活动申请（含 activity_level、version、总结字段）** |
| **`activity_approve_flow`** | **活动审批步骤（含 flow_type、step_enter_time）** |
| **`activity_apply_history`** | **变更前快照 + 变更后目标值** |
| `activity_sign` | 活动签到 |
| `notice_*` | 通知分类、内容、阅读记录 |
| `club_statistics` | 社团日统计 |

> 活动审批增量 DDL 参考：`mysql/activity_approval_migration.sql`（`activity_level`、`version`、`attachment`、`summary_*`、`activity_apply_history` 等）。

### 2.2A `club_application` 状态

| status | 含义 |
|---|---|
| 1 | 待学院审批 |
| 2 | 学院已通过 |
| 3 | 已通过（校级终审通过） |
| 4 | 已驳回 |
| 5 | 已撤回 |

`apply_type`：1=创建，2=解散。

### 2.2B `club_council` 状态

| status | 含义 |
|---|---|
| 1 | 合议中 |
| 2 | 已通过（已执行解散） |
| 3 | 已驳回 |

### 2.2C `activity_apply` 审批状态

| approve_status | 含义 |
|---|---|
| 1 | 草稿 |
| 2 | 待审批 |
| 3 | 审批中 |
| 4 | 已通过 |
| 5 | 已驳回 |
| 6 | 已取消 |
| 7 | 变更审批中 |

`activity_level`：1=院级，2=校级。`version`：乐观锁，审批/变更/取消/总结均需携带。

`activity_approve_flow.flow_type`：1=正常审批，2=变更审批。

### 2.3 实体时间字段序列化

所有含 `createTime` / `updateTime`（及其他 `time` 类字段）的实体类已统一添加：

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
```

涉及实体：`ClubApplication`、`ClubCouncil`、`SysUser`、`SysRole`…（其余同前）。

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
├── security/            (16+)      # 含 ActivityApproverHelper、ActivityApprovalChainHelper、ClubSecurityHelper 等
├── filter/              (2)        # JwtFilter、IgnorePathsProperties
├── task/                           # ActivityApprovalTimeoutTask 定时任务
├── converter/           (2)        # MapStruct：SysUserConverter、SysRoleConvert
├── advice/                         # GlobalExceptionHandler
├── exception/                      # EIException、ErrorConfig、Usual、Level 常量
└── utils/                          # R、JwtUtil、MPUtil、SecurityUtils、ClubCodeGeneratorUtil 等
```

### 3.2 权限三层分离（当前架构）

| 层级 | 职责 | 实现位置 |
|---|---|---|
| **准入鉴权** | 控制哪些等级可调用接口 | `@RequiresLevel` + `LevelAspect` |
| **数据行过滤** | 按等级拼接查询条件 | `DataScopeHelper`（用户模块）、`LevelBasedAccess`（通用等级过滤） |
| **字段脱敏** | 低权限隐藏敏感 DTO 字段 | `FieldMaskHelper` + `UserSecurityHelper.toMaskedDto` |

> 设计原则：同一业务仅保留单一 Controller 接口；`@RequiresLevel` 不做数据/字段差异化。  
> **已完成重构**：`SysUserController`、`SysRoleController`、`SysUserRoleController`、`SysMenuController`、`SysRoleMenuController`、**`ClubApplicationController`**、**`ClubCouncilController`**、**`ActivityApplyController`**  
> **待重构**：`ActivityApproveFlowController` 等仍保留 `_F` / `_B` 双接口（活动审批流已内聚于 `ActivityApplyService`）。

### 3.3 权限等级常量（`untiy.exception.Level`）

| 常量 | 值 | 含义 |
|---|---|---|
| `SUPER_ADMIN` | 0 | 超级管理员，可见全部 |
| `ADMIN` | 1 | 管理员/导员，可见全部学生 |
| `CLUB_LEADER` | 2 | 社长，可见本社团 |
| `DEPT_LEADER` | 3 | 部长，可见本部门 |
| `STUDENT` | 4 | 普通学生，仅可见自己 |

`effectiveLevel` 由 `UserScopeResolver` 根据 `sys_role.role_code` 解析（取所有角色中最高权限，即数值最小）。**`ADVISOR*` 前缀角色码映射为等级 1**；未命中内置映射时回退 `role_level`。

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
| **`MenuTreeHelper`** | **菜单树内存组装**：纯静态、一次遍历建索引挂接，禁止递归查库；`buildTree`、`expandWithAncestors`、`collectPermissionCodes`、`pruneEmptyDirectories` |
| **`MenuCacheHelper`** | **菜单树 Redis 缓存**：Key `menu:tree:{userId}`（非 effectiveLevel）；`get/put/evictAll`，TTL 1 小时 |
| **`ClubSecurityHelper`** | **社团申请/合议**：指导老师、院长、校级管理员、学院范围校验 |
| **`ClubDissolveExecutor`** | **执行解散**：社团状态、部门、活动清理、角色解绑（进行中活动含 status 7） |
| **`ActivityApproverHelper`** | **活动审批**：发起人识别、社长/指导老师/学院书记/校书记查找、超时转交 |
| **`ActivityApprovalChainHelper`** | **活动审批链**：按发起人角色 + 院/校级动态生成步骤；变更链固定 3 步 |
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
| **菜单树 Key** | **`menu:tree:{userId}`** → `MenuTreeResultVO`；变更后全量清除 |

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

#### `UserCacheHelper`（Redis 登录缓存）

| 方法 | 用途 |
|---|---|
| `evictByUsernames(redisTemplate, usernames)` | 批量删除 `user:{username}` 缓存（禁用/启用后强制重新加载） |

#### `MenuTreeHelper` / `MenuCacheHelper`（菜单树）

| 类 | 要点 |
|---|---|
| `MenuTreeHelper` | 从 flat 列表组装树；排除 `menu_type=3` 进树；按 `sort` 排序；修剪空目录；补全祖先节点 |
| `MenuCacheHelper` | 按 **userId** 缓存 `MenuTreeResultVO`；菜单/角色菜单增删改后 `evictAll` |

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

## 9. 异常码（`ErrorConfig` / `Usual`）

> 编码规范见 `.cursor/skills/Erroconfig.md`。通用 HTTP 常量定义在 **`Usual.java`**，`ErrorConfig` 引用并扩展业务码。

### 9.1 编码区间

| 编码段 | 模块 | 说明 |
|---|---|---|
| `400` | 通用 | `Usual.BAD_REQUEST` → `ErrorConfig.BAD_REQUEST` |
| `1xxx` | 用户 | 注册、用户名、无权操作用户（1009） |
| `6xxx` | 活动 | 活动分类/内容/编号 |
| `7xxx` | 通用校验 | 非法字符等 |
| `8xxx` | 认证/Token | 未登录 8001、Token 过期 8003 等 |
| `9xxx` | 角色权限 | 9001~9014 |
| `91xx` | 菜单 | 9101~9108 |
| `92xx` | 社团申请/合议 | 9201~9218 |
| **`93xx`** | **活动审批** | **9301~9314** |

### 9.2 角色权限（9xxx）

| Code | 常量 | 消息 |
|---|---|---|
| 9001 | `ROLE_NOT_FOUND` | 角色不存在 |
| 9002 | `ROLE_LEVEL_INSUFFICIENT` | 无权创建高于自身等级的角色 |
| 9003 | `ROLE_CANNOT_MODIFY_HIGHER` | 不能修改更高等级的角色 |
| 9004 | `ROLE_CANNOT_ELEVATE` | 不能提升自身权限等级 |
| 9005 | `ROLE_IN_USE` | 不能删除当前用户正在使用的角色 |
| 9006 | `ROLE_NO_PERMISSION` | 无权查看该角色 |
| 9007 | `ROLE_CODE_BLANK` | 角色编码不能为空 |
| 9008 | `USER_DISABLED` | 用户已被禁用，无法操作 |
| 9009 | `CANNOT_DISABLE_SELF` | 不能禁用当前登录账号 |
| 9010 | `BATCH_CONTAINS_DISABLED` | 批量操作中包含已禁用用户，已拒绝 |
| 9011 | `ROLE_ASSIGN_DUPLICATE` | 角色分配冲突 |
| 9012 | `ROLE_SCOPE_INVALID` | 角色数据范围不合法 |
| 9013 | `ROLE_REVOKE_SELF` | 不能撤销自己当前持有的角色 |
| 9014 | `USER_ROLE_NOT_FOUND` | 用户角色关联不存在 |

### 9.3 菜单管理（91xx）

| Code | 常量 | 消息 |
|---|---|---|
| 9101 | `MENU_NOT_FOUND` | 菜单不存在 |
| 9102 | `MENU_PARENT_NOT_FOUND` | 父菜单不存在 |
| 9103 | `MENU_CYCLE` | 不能将菜单挂到自身或子级下 |
| 9104 | `MENU_NAME_DUPLICATE` | 同级菜单名称已存在 |
| 9105 | `MENU_HAS_CHILDREN` | 存在子菜单，无法删除 |
| 9106 | `MENU_BOUND_HIGHER_ROLE` | 该菜单已被更高权限角色绑定 |
| 9107 | `MENU_COMPONENT_REQUIRED` | 页面类型组件路径不能为空 |
| 9108 | `MENU_IDS_INVALID` | 部分菜单不存在或已失效 |

### 9.4 社团申请 / 合议（92xx）

| Code | 常量 | 消息 |
|---|---|---|
| 9201 | `CLUB_APPLY_NOT_FOUND` | 社团申请不存在 |
| 9202 | `CLUB_NOT_FOUND` | 社团不存在 |
| 9203 | `CLUB_NOT_NORMAL` | 社团状态异常，无法操作 |
| 9204 | `CLUB_NAME_DUPLICATE` | 同一学院下社团名称已存在 |
| 9205 | `CLUB_NOT_ADVISOR` | 当前用户不是该社团指导老师 |
| 9206 | `CLUB_HAS_ACTIVE_ACTIVITY` | 社团存在进行中的活动，无法解散 |
| 9207 | `CLUB_APPLY_STATUS_INVALID` | 申请状态不允许此操作 |
| 9208 | `CLUB_NOT_DEAN` | 仅学院负责人可审批该申请 |
| 9209 | `CLUB_CANNOT_APPROVE_SELF` | 不能审批自己提交的申请 |
| 9210 | `CLUB_COUNCIL_NOT_FOUND` | 合议记录不存在 |
| 9211 | `CLUB_COUNCIL_IN_PROGRESS` | 该社团已有进行中的合议 |
| 9212 | `CLUB_COUNCIL_NOT_IN_PROGRESS` | 合议不在进行中 |
| 9213 | `CLUB_ALREADY_SIGNED` | 您已签字，不能重复签字 |
| 9214 | `CLUB_COLLEGE_OUT_OF_SCOPE` | 不在该学院管理范围内 |
| 9215 | `CLUB_ROLE_NOT_FOUND` | 系统角色配置缺失 |
| 9216 | `CLUB_PROPOSED_LEADER_INVALID` | 拟定社长不存在或已被禁用 |
| 9217 | `CLUB_REQUIRE_ADVISOR_ROLE` | 需要指导老师角色 |
| 9218 | `CLUB_REQUIRE_ADMIN_ROLE` | 需要校级管理员权限 |

| 9218 | `CLUB_REQUIRE_ADMIN_ROLE` | 需要校级管理员权限 |

### 9.5 活动审批（93xx）

| Code | 常量 | 消息 |
|---|---|---|
| 9301 | `ACT_APPLY_NOT_FOUND` | 活动申请不存在 |
| 9302 | `ACT_CLUB_NOT_FOUND` | 主办社团不存在或状态异常 |
| 9303 | `ACT_SUBMIT_NO_PERMISSION` | 当前用户无权发起活动申请 |
| 9304 | `ACT_LEVEL_INVALID` | 活动级别必须为院级或校级 |
| 9305 | `ACT_STATUS_INVALID` | 活动状态不允许此操作 |
| 9306 | `ACT_NOT_CURRENT_APPROVER` | 您不是当前步骤审批人 |
| 9307 | `ACT_OPINION_REQUIRED` | 审批意见不能为空 |
| 9308 | `ACT_VERSION_CONFLICT` | 数据已被修改，请刷新重试 |
| 9309 | `ACT_APPROVER_NOT_FOUND` | 无法确定审批人，请检查角色配置 |
| 9310 | `ACT_NOT_APPLICANT` | 仅申请人可执行此操作 |
| 9311 | `ACT_CHANGE_FIELDS_INVALID` | 变更仅允许修改时间或地点 |
| 9312 | `ACT_SUMMARY_WINDOW` | 活动总结须在结束后1-3天内上传 |
| 9313 | `ACT_TIME_INVALID` | 开始时间必须早于结束时间 |
| 9314 | `ACT_LEVEL_ADJUST_LOCKED` | 活动级别已调整过，不可再次修改 |

### 9.6 认证 / 用户（节选）

| Code | 常量 | 消息 |
|---|---|---|
| 400 | `BAD_REQUEST` | 请求参数无效（`Usual`） |
| 8001 | `LOGIN_INVALID` / `NOT_LOGGED_IN` | 未登录或会话已失效 |
| 1009 | `NO_PERM_DELETE_USER` | 无权改变用户: |

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

### 10.5 `SysMenuServiceImpl`

| 方法 | 说明 |
|---|---|
| `getMenuTreeForCurrentUser()` | 角色→`sys_role_menu`→菜单；补祖先；`MenuTreeHelper` 建树；`permissions` 集合；Redis 缓存 |
| `pageQuery` | `menuName` 模糊 + `menuType` 过滤；`MPUtil.getPage` 平铺分页 |
| `saveMenu` | 父级/循环/同级名/类型字段校验；高等级角色绑定拦截；`MenuCacheHelper.evictAll` |
| `deleteMenu` | 有子菜单拒绝；高等级角色绑定拒绝；清理 `sys_role_menu`；清缓存 |

### 10.6 `SysRoleMenuServiceImpl`（`@Slf4j`）

| 方法 | 说明 |
|---|---|
| `assign(AssignRoleMenuDTO)` | 校验角色存在；`checkOperable` 防越权；校验 `menuIds` 均为 `status=1`；**先删后增**；`MenuCacheHelper.evictAll` |
| `listMenuIdsByRole(roleId)` | 返回已绑定 `menu_id` 列表（去重排序） |

### 10.7 `ClubApplicationServiceImpl`（`@Slf4j`）

| 方法 | 说明 |
|---|---|
| `createApply` | 指导老师角色校验；拟定社长未禁用；同学院社团名唯一；生成 `application_no`；status=待学院审批 |
| `dissolveApply` | 须为社团 `advisor_id`；社团正常；无进行中活动；写入解散申请 |
| `pageQuery` | 按申请人/状态/类型 + 时间范围分页 |
| `getDetail` | 申请详情 + `currentApprover`（院长/校级管理员） |
| `approveCollege` | 院长 `dean_id` 校验；不可审自己；通过→学院已通过 / 驳回 |
| `approveAdmin` | 校级管理员校验；创建通过→**激活社团**+绑定 `CLUB_PRESIDENT`；解散通过→**执行解散** |

### 10.8 `ClubCouncilServiceImpl`（`@Slf4j`）

| 方法 | 说明 |
|---|---|
| `initiate` | 超管；社团正常；学院范围内；无进行中合议/活动；status=合议中 |
| `sign` | 超管/ADMIN 签字；学院范围；防重复；达成条件（1超管+2ADMIN 或 3超管）→ `ClubDissolveExecutor` |

### 10.9 `ActivityApplyServiceImpl`（`@Slf4j`）

| 方法 | 说明 |
|---|---|
| `submit` | 校验发起人角色（部长/社长/指导老师/学院书记/ADMIN）；生成 `activity_no`；`approve_status=2`、`step=1`；动态写入 `activity_approve_flow` |
| `approve` | 当前步骤审批人；必填意见；指导老师可调整 `activity_level` 一次；推进步骤或 `status=4`；乐观锁 |
| `reject` | 驳回终止；变更流驳回则恢复 `status=4` 并标记 history 已驳回 |
| `requestChange` | 仅 `status=4`；快照旧数据至 `activity_apply_history`（含 new_* 目标值）；`status=7`；变更链：社长→指导老师→学院书记 |
| `cancel` | 仅申请人；无需审批；`status=6` |
| `uploadSummary` | 已通过活动；结束后 1~3 天；文字 + 附件 |
| `getDetail` | 申请 + 审批流 + 变更历史 + 当前审批人 |
| `pageQuery` | 分页条件查询 |

**审批链路径（正常申请）：**

| 发起人 | 步骤序列 | 校级额外 |
|---|---|---|
| 部长 | 社长 → 指导老师 → 学院书记 | + 校书记（SUPER_ADMIN） |
| 社长 | 指导老师 → 学院书记 | + 校书记 |
| 指导老师 | 学院书记 | + 校书记 |
| 学院书记/ADMIN | 指导老师 → 学院书记 | + 校书记 |

**审批人查找：**

| 角色 | 规则 |
|---|---|
| 社长 | `sys_user_role`：`CLUB_PRESIDENT` + `scope_type=2` + `scope_id=clubId` |
| 指导老师 | `sys_club.advisor_id` |
| 学院书记 | `sys_college.dean_id`（社团挂靠学院） |
| 校书记 | `SUPER_ADMIN` 角色用户 |

**超时（`ActivityApprovalTimeoutTask`）：** 每小时扫描；步骤进入 >3 天日志催办；>7 天转交上级（社长→指导老师→学院书记→校书记）。

---

## 10A. 实体 / DTO / VO / Mapper

### DTO（`entity/dto/`）

| 类 | 字段 | 用途 |
|---|---|---|
| `AssignRoleDTO` | `userId`、`roleId`、`scopeType`、`scopeId` | `POST /sys-user-role/assign`；scope 须符合目标角色 `data_scope` |
| `ToggleStatusDTO` | `usernames`、`status`（0/1） | `PUT /sys-user/toggleStatus` |
| `AssignRoleMenuDTO` | `roleId`、`menuIds` | `POST /sys-role-menu/assign`；空列表=清空绑定 |
| `ClubCreateApplyDTO` | `clubName`、`collegeId`、`category`、`description`、`proposedLeaderId`、`maxMembers` | `POST /club-application/apply/create` |
| `ClubDissolveApplyDTO` | `clubId`、`dissolveReason` | `POST /club-application/apply/dissolve` |
| `ClubApproveDTO` | `approved`、`opinion` | 学院/校级审批 |
| `CouncilInitiateDTO` | `clubId`、`reason` | `POST /club-council/council/initiate` |
| **`ActivitySubmitDTO`** | `clubId`、`activityName`、`categoryId`、`activityType`、`activityLevel`、时间地点、预算、内容、附件 | `POST /activity-apply/submit` |
| **`ActivityApproveDTO`** | `version`、`opinion`、`activityLevel?` | 审批通过/驳回 |
| **`ActivityChangeDTO`** | `version`、时间地点、`changeReason` | 变更申请 |
| **`ActivityCancelDTO`** | `version`、`reason` | 取消活动 |
| **`ActivitySummaryDTO`** | `version`、`summaryContent`、`summaryAttachment` | 活动总结 |

### VO（`entity/vo/`）

| 类 | 字段 | 用途 |
|---|---|---|
| `SysUserRoleVO` | … | 用户-角色联查展示 |
| `MenuTreeVO` / `MenuTreeResultVO` | … | 菜单树 |
| `ClubApplicationDetailVO` | `application`、`currentApprover`、`queryTime` | 申请详情 |
| `CouncilSignRecordVO` | `userId`、`roleCode`、`level`、`signTime` | 合议签字 JSON 元素 |

### 常量 / 工具

| 类 | 说明 |
|---|---|
| `ClubApplyConstants` | 申请类型、状态、合议状态、scope 类型、角色码常量 |
| **`ActivityApplyConstants`** | 活动状态、级别、审批流类型、发起人/审批人类型、历史状态 |
| `ClubCodeGeneratorUtil` | 生成 `APP*` 申请编号、`CLUB*` 社团编号（防重复） |
| **`ActivityCodeGeneratorUtil`** | 生成 `ACT*` 活动编号（按分类后缀 + 防重复） |
| **`ActivityFileStorageUtil`** | 活动申请/总结附件本地存储 |

### Mapper

| 接口 / XML | 方法 | 说明 |
|---|---|---|
| `SysUserRoleMapper.selectPageWithDetail` | 分页联查 | JOIN `sys_user`、`sys_role`；按 `userIds` + `keyword` |
| `SysUserRoleMapper.selectListByUserId` | 列表 | 当前用户全部角色关联 |

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

### 11.5 `SysMenuController`（已统一为 4 接口）

根路径：`/sys-menu`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/tree` | GET | `STUDENT (4)` | 当前用户菜单树 + `permissions`；Redis `menu:tree:{userId}` |
| `/list` | GET | `ADMIN (1)` | 全量分页；`menuName` 模糊 + `menuType` 过滤 |
| `/save` | POST | `ADMIN (1)` | 新增/更新；校验父级、循环、同级名、类型字段 |
| `/delete/{id}` | DELETE | `ADMIN (1)` | 无子菜单且可操作时删除；清理 `sys_role_menu` |

**已移除的旧接口：** `listSysMenu`、`listSysMenu_F/B`、`query`、`detailSysMenu_F/B`、`add_B`、`updateSysMenu_B`、`deleteSysMenu_B`

### 11.6 `SysRoleMenuController`（已统一为 2 接口）

根路径：`/sys-role-menu`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/assign` | POST | `ADMIN (1)` | 全量覆盖分配；Body: `AssignRoleMenuDTO`；先删后增 |
| `/listByRole/{roleId}` | GET | `ADMIN (1)` | 返回已绑定 `menu_id` 列表 |

**已移除的旧接口：** `listSysRoleMenu`、`listSysRoleMenu_F/B`、`query`、`detailSysRoleMenu_F/B`、`add_B`、`updateSysRoleMenu_B`、`deleteSysRoleMenu_B`

### 11.7 `ClubApplicationController`（8 接口）

根路径：`/club-application`（规范见 `.cursor/skills/活动.md`）

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/apply/create` | POST | `ADMIN (1)` | 指导老师发起创建申请；Service 校验 `ADVISOR*` 角色 |
| `/apply/dissolve` | POST | `ADMIN (1)` | 社团指导老师发起解散申请 |
| `/apply/list` | GET | `ADMIN (1)` | 分页；申请人/状态/类型/时间范围 |
| `/apply/detail/{id}` | GET | `ADMIN (1)` | 详情 + 当前审批人 |
| `/approve/college/{id}` | POST | `ADMIN (1)` | 学院院长审批；Body: `ClubApproveDTO` |
| `/approve/admin/{id}` | POST | `ADMIN (1)` | 校级审批；创建→激活社团，解散→执行解散 |

### 11.8 `ClubCouncilController`（2 接口）

根路径：`/club-council`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/council/initiate` | POST | `SUPER_ADMIN (0)` | 超管发起合议解散；Body: `CouncilInitiateDTO` |
| `/council/sign/{id}` | POST | `ADMIN (1)` | 超管/校级管理员签字；达成条件自动解散 |

### 11.9 `ActivityApplyController`（10 接口）

根路径：`/activity-apply`（规范见 `.cursor/skills/活动审批模块.md`）

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/submit` | POST | `ADMIN (1)` | 提交活动申请；动态生成审批链 |
| `/upload/attachment` | POST | `ADMIN (1)` | 上传申请附件；返回相对路径 |
| `/upload/summary` | POST | `ADMIN (1)` | 上传总结附件 |
| `/approve/{id}` | POST | `ADMIN (1)` | 审批通过；Body: `ActivityApproveDTO`（含 `version`） |
| `/reject/{id}` | POST | `ADMIN (1)` | 审批驳回；必填意见 |
| `/change/{id}` | POST | `ADMIN (1)` | 已通过活动变更时间/地点 |
| `/cancel/{id}` | POST | `ADMIN (1)` | 申请人取消活动 |
| `/summary/{id}` | POST | `ADMIN (1)` | 上传活动总结（结束后 1~3 天） |
| `/list` | GET | `ADMIN (1)` | 分页列表 |
| `/detail/{id}` | GET | `ADMIN (1)` | 详情（审批流 + 变更历史） |

**已移除的旧接口：** `listActivityApply_F/B`、`detailActivityApply_F/B`、`add_B`、`updateActivityApply_B`、`deleteActivityApply_B`、`query`

> `ActivityApproveFlowController` 仍保留旧 CRUD，新业务不依赖；审批流由 `ActivityApplyService` 内部维护。

### 11.10 其他 Controller 根路径

| Controller | 根路径 |
|---|---|
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
登录拦截：UserDetailServiceImpl → status≠1 → USER_DISABLED(9008)
批量更新：updateUsers → assertBatchNoDisabled → 含禁用用户整批拒绝
角色分配：POST /sys-user-role/assign → findActiveInScope → validateScope(role.dataScope) → 防重复
角色撤销：DELETE /sys-user-role/revoke/{id} → checkOperable → 不可撤销自身角色
```

### 13.4 菜单与角色菜单

```
菜单树：GET /sys-menu/tree → 用户角色 → sys_role_menu → 菜单+祖先 → MenuTreeHelper → Redis 缓存
菜单维护：POST /save、DELETE /delete/{id} → 校验 → MenuCacheHelper.evictAll
角色菜单：POST /sys-role-menu/assign → checkOperable → 校验 menuIds → 先删后增 → evictAll
```

### 13.5 社团创建 / 解散 / 合议

```
创建：POST /apply/create → 指导老师 → 同学院名唯一 → APP编号 → 待学院审批
      → POST /approve/college → dean_id → 学院已通过
      → POST /approve/admin → 校级 → 插入 sys_club + 绑定 CLUB_PRESIDENT(scope_type=2)

解散(申请)：POST /apply/dissolve → advisor_id → 无进行中活动 → 审批链同上 → 校级通过 → ClubDissolveExecutor

合议：POST /council/initiate → 超管+学院范围 → POST /council/sign → 签字 JSON
      → 1超管+2ADMIN 或 3超管 → 执行解散

ClubDissolveExecutor：社团 status=0；删部门；活动仅保留已通过且已结束；移除社长/部长角色绑定；进行中活动含 status 1/2/3/7
```

### 13.2 活动审批

```
提交：POST /activity-apply/submit → 识别发起人角色 → ActivityApprovalChainHelper 生成步骤
      → ActivityApproverHelper 预分配 approve_user_id → activity_approve_flow
      → approve_status=2, current_approve_step=1, version=0

审批：POST /approve/{id} 或 /reject/{id} → 校验当前步骤审批人 + version
      → 通过：写 flow 记录 → 无下一步则 status=4，否则 step++ 且 status=3
      → 驳回：status=5（变更流则 status=4 + history 标记驳回）
      → 指导老师审批时可调整 activity_level 一次（level_adjust_locked）

变更：POST /change/{id} → 仅 status=4 → 快照至 activity_apply_history（含 new_* 字段）
      → status=7 → 变更链：社长 → 指导老师 → 学院书记
      → 通过后主表写入 new_*；驳回主表不变

取消：POST /cancel/{id} → 仅申请人 → status=6

总结：POST /summary/{id} → 已通过 + 结束后 1~3 天 → summary_content / summary_attachment

超时：ActivityApprovalTimeoutTask @Scheduled 每小时 → 3 天催办日志 → 7 天转交上级
```

### 13.3 通知（概要）

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
        SV --> MTH[MenuTreeHelper]
        SV --> MCH[MenuCacheHelper]
        SV --> CSH[ClubSecurityHelper]
        SV --> CDE[ClubDissolveExecutor]
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
| ✅ 已修复 | `data_scope` 分配无效 | `validateScope` 改为读取 `role.getDataScope()` |
| ✅ 已完成 | 菜单模块单接口化 | `SysMenuController` 4 接口 + `MenuTreeHelper` + `MenuCacheHelper` |
| ✅ 已完成 | 角色-菜单分配 | `SysRoleMenuController` 2 接口；全量覆盖 `assign` |
| ✅ 已完成 | 异常码分段重构 | `9xxx` 角色、`91xx` 菜单、`92xx` 社团；通用码迁入 `Usual.java` |
| ✅ 已完成 | 社团申请/合议模块 | `ClubApplicationController` + `ClubCouncilController`；见 `活动.md` |
| ⏳ 待做 | 种子角色 `CLUB_PRESIDENT` | 创建申请校级通过后绑定社长依赖该角色 |
| ⏳ 待做 | 其余 Controller `_F/_B` 合并 | 含 `activity-*` 等待重构 |
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
| **异常码规范** | `.cursor/skills/Erroconfig.md` | 编码区间与常量命名 |
| **社团申请/合议** | `.cursor/skills/活动.md` | 创建/解散/审批/合议业务规范 |
| 前端开发规范 | `.cursor/skills/skill.md` | Vue3/Element Plus |
| OpenAPI JSON | `src/api/spec/api-docs.json` | 机器可读 API |
| 数据库脚本 | `mysql/mass_test1.sql` | 建表 + 种子 |

---

## 17. 近期变更文件索引（2026-07-04）

> RBAC 与社团生命周期模块核心变更，按包路径归类。

### 17.1 Controller

| 文件 | 变更摘要 |
|---|---|
| `controller/SysUserController.java` | 单接口化；`toggleStatus`、`listDisabled` |
| `controller/SysUserRoleController.java` | 4 接口：assign / revoke / list / my-roles |
| `controller/SysMenuController.java` | 4 接口：tree / list / save / delete |
| `controller/SysRoleMenuController.java` | 2 接口：assign / listByRole |
| **`controller/ClubApplicationController.java`** | **8 接口：apply/create、dissolve、list、detail、approve/college、approve/admin** |
| **`controller/ClubCouncilController.java`** | **2 接口：council/initiate、council/sign** |

### 17.2 DTO（`entity/dto/`）

| 文件 | 变更摘要 |
|---|---|
| `AssignRoleDTO.java` | 用户-角色分配 |
| `ToggleStatusDTO.java` | 批量启用/禁用 |
| `AssignRoleMenuDTO.java` | 角色-菜单全量分配 |
| **`ClubCreateApplyDTO.java`** | 社团创建申请 |
| **`ClubDissolveApplyDTO.java`** | 社团解散申请 |
| **`ClubApproveDTO.java`** | 学院/校级审批 |
| **`CouncilInitiateDTO.java`** | 合议发起 |

### 17.3 VO（`entity/vo/`）

| 文件 | 变更摘要 |
|---|---|
| `SysUserRoleVO.java` | 用户-角色联查 |
| `MenuTreeVO.java` / `MenuTreeResultVO.java` | 菜单树 |
| **`ClubApplicationDetailVO.java`** | 申请详情 + 当前审批人 |
| **`CouncilSignRecordVO.java`** | 合议签字记录 |

### 17.4 Exception / 常量

| 文件 | 变更摘要 |
|---|---|
| `exception/ErrorConfig.java` | `9xxx`/`91xx`/`92xx` 分段 |
| `exception/Usual.java` | 通用 HTTP 常量 |
| **`entity/constants/ClubApplyConstants.java`** | 申请/合议状态与类型常量 |

### 17.5 Mapper

| 文件 | 变更摘要 |
|---|---|
| `mapper/SysUserRoleMapper.java` + XML | 用户-角色联查 |
| **`mapper/ClubApplicationMapper.java`** | 社团申请 CRUD（MyBatis-Plus） |
| **`mapper/ClubCouncilMapper.java`** | 合议 CRUD（MyBatis-Plus） |
| **`mapper/ActivityApplyHistoryMapper.java`** | 活动变更历史快照 |

### 17.6 Security

| 文件 | 变更摘要 |
|---|---|
| `security/DataScopeHelper.java` | 用户行级过滤 |
| `security/LevelBasedAccess.java` | 全局等级控制 |
| `security/UserSecurityHelper.java` | 用户范围/状态 |
| `security/UserCacheHelper.java` | 登录 Redis 清理 |
| `security/UserRoleScopeHelper.java` | 角色分配 scope |
| `security/MenuTreeHelper.java` / `MenuCacheHelper.java` | 菜单树 |
| **`security/ClubSecurityHelper.java`** | 指导老师/院长/学院范围/校级管理员 |
| **`security/ClubDissolveExecutor.java`** | 解散执行（社团/部门/活动/角色） |
| **`security/UserScopeResolver.java`** | 支持 `ADVISOR*` 前缀映射等级 1 |

### 17.7 Service 接口

| 文件 | 变更摘要 |
|---|---|
| `service/SysUserService.java` | `toggleStatus`、`listDisabled` |
| `service/SysUserRoleService.java` | assign / revoke / pageQuery / listMyRoles |
| `service/SysMenuService.java` | 菜单树与 CRUD |
| `service/SysRoleMenuService.java` | 角色菜单 assign |
| **`service/ClubApplicationService.java`** | 创建/解散/列表/详情/学院审批/校级审批 |
| **`service/ClubCouncilService.java`** | 合议发起/签字 |

### 17.8 Service 实现

| 文件 | 变更摘要 |
|---|---|
| `service/impl/SysUserServiceImpl.java` | 用户状态管理 |
| `service/impl/SysUserRoleServiceImpl.java` | 角色分配 |
| `service/impl/UserDetailServiceImpl.java` | 登录禁用拦截 |
| `service/impl/SysMenuServiceImpl.java` | 菜单模块 |
| `service/impl/SysRoleMenuServiceImpl.java` | 角色菜单 |
| **`service/impl/ClubApplicationServiceImpl.java`** | 社团申请全流程；`@Slf4j` |
| **`service/impl/ClubCouncilServiceImpl.java`** | 合议签字与条件判定；`@Slf4j` |

### 17.9 Utils / Entity

| 文件 | 变更摘要 |
|---|---|
| **`utils/ClubCodeGeneratorUtil.java`** | 申请号、社团编号生成 |
| **`entity/ClubApplication.java`** | 对应 `club_application` 表 |
| **`entity/ClubCouncil.java`** | 对应 `club_council` 表（`signatories` JSON） |

---

*文档结束*
