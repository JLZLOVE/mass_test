# Mass_Test 项目概览文档

> 本文档基于 `.cursor/skills/更新信息.md` 扫描结果维护，用于团队沟通与后续重构参考。  
> 更新时间：2026-07-16（角色分配 PRESIDENT/ADMIN 校验、用户删除权限校验 UserPermissionUtils、SysUserController 详情/删除改用 username、SysUserMapper 新增 deleteByUsername）  
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
| **通知附件目录** | `notice.upload-dir: uploads/notice`（本地存储，预留 OSS） |
| **定时任务** | `@EnableScheduling`；活动审批超时每小时；通知每分钟；签到自动签退每5分钟 |

### 1.4 根目录结构

```
Mass_Test/
├── pom.xml
├── PROJECT_OVERVIEW.md             # 根目录副本
├── src/                            # 后端源码
├── club-admin-frontend/            # Vue 3 管理端
├── mysql/                          # 数据库脚本（含 *_migration.sql）
└── .cursor/skills/
    ├── PROJECT_OVERVIEW.md         # 本文档
    ├── 更新信息.md                 # 文档更新扫描清单
    ├── 项目.md                     # 项目分析指令
    ├── login.md                    # JWT/登录规范
    ├── jwt.md                      # JWT 安全重构说明
    ├── 权限过滤.md                 # 五级可见范围与权限过滤方案
    ├── 活动.md                     # 社团创建/解散/合议业务流程
    ├── 活动审批模块.md             # 活动申请/审批/变更/取消/总结/超时
    ├── 通知.md                     # 通知发送/撤回/已读/模板/定时
    ├── 签到.md                     # 活动签到/签退/补签/统计
    ├── id.md                       # 对外 username / 对内 userId 暴露规范
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
活动：activity_category → activity_apply → activity_approve_flow / activity_apply_history / activity_sign_config / activity_sign / activity_sign_makeup
通知：notice_category → notice_info → notice_read_record；notice_template（模板）
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
| **`activity_sign`** | **签到记录（定位/扫码/补签/手动）** |
| **`activity_sign_config`** | **签到配置（方式/窗口/半径/签退/二维码）** |
| **`activity_sign_makeup`** | **补签申请与审批** |
| **`notice_info`** | **通知（含置顶、长期可见、附件权限）** |
| **`notice_read_record`** | **已读/确认记录** |
| **`notice_template`** | **通知模板（`template_name` 存编码，见 §2.2F）** |
| `notice_category` | 通知分类 |
| `club_statistics` | 社团日统计 |

> 活动审批增量 DDL：`mysql/activity_approval_migration.sql`  
> 通知模块增量 DDL：`mysql/notice_module_migration.sql`  
> **签到模块增量 DDL：`mysql/activity_sign_migration.sql`**

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

`activity_level`：1=院级，2=校级。`version`：乐观锁。

### 2.2D `notice_info` 状态

| status | 含义 |
|---|---|
| 0 | 草稿 |
| 1 | 已发布 |
| 2 | 已撤回 |

`receiver_type`：1全体学生 2全体老师 3指定角色 4指定社团 5指定人员。  
`importance`：1高 2中 3低；`urgency`：1紧急 2不紧急（任一为1则置顶加红）。  
`long_term_visible`：1动态接收人，0发布时固化 `notice_read_record`。  
`attachment_min_level`：0~4，用户 `effectiveLevel ≤ 该值` 可见附件。

### 2.2F `notice_template.template_name`（模板编码）

| 项 | 说明 |
|---|---|
| 存储列 | **`template_name`**（不新增 `template_code` 列） |
| 格式 | `{前缀}_{yyyyMMddHHmm}_{6位随机数}`，例 `NOTICE_202607161430_827391` |
| 前缀 | `TemplateCodePrefix`：`NOTICE` / `CLUB` / `ACT` |
| 生成 | `TemplateCodeUtil.generate(prefix, createTime)` |
| 防篡改 | 解析编码中分钟段，与 `create_time` 截断到分钟后比对；不一致抛 **6412** |
| 对外 API | 创建返回 `templateName`；详情/更新/删除用 `?templateName=`；**不暴露 id** |

### 2.2G 对外 username 暴露约定（`.cursor/skills/id.md`）

| 规则 | 说明 |
|---|---|
| 用户标识 | 外部 API 统一 **`username`（学号/工号）**；内部 DB 仍用 `userId` |
| 实体 | 用户 FK 字段 `@JsonIgnore`；响应通过 `*Username` 虚拟字段（`UserExposeHelper`） |
| 路径参数 | **禁止**在 URL 路径中放 `username`（中文工号如「玖」会导致代理 `unescaped characters`）；用 Query / Body |
| 超管账号 | `玖`（username）、`1001`（另一超管 username）均为 `sys_user.username`，非主键 id |

### 2.2E 签到模块要点

| 项 | 说明 |
|---|---|
| `activity_sign_config.sign_mode` | 1定位 2扫码 3两者 |
| `activity_sign.sign_type` | 1定位 2扫码 3补签 4手动 |
| 去重 | `uk_activity_user(activity_id, user_id)` |
| 补签审批 | 院级：指导老师；校级：指导老师 → 学院书记 |
| 自动签退 | 活动结束后未签退自动补签退（不标记早退） |

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
├── security/            (17+)      # 含 ActivityApproverHelper、ActivityApprovalChainHelper、ClubSecurityHelper、UserPermissionUtils 等
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
> **已完成重构**：全部 Controller 已统一为单一接口，无残留 `_F/_B` 旧接口。

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
4. Redis Key **`user:v2:{username}`**：读 `LoginUserDetails.CacheSnapshot`（`@JsonProperty` 全字段）；旧版 `Collection` 或脏数据 → **删除 Key 后回源 DB**  
5. `SecurityContext` principal = `LoginUserDetails`

---

## 7. 安全模块（`security` 包）

| 类 | 职责 |
|---|---|
| `SecurityConfig` | 无状态 Session；仅 Swagger/静态 `permitAll()`；`AuthenticationEntryPoint` 返回 401 JSON；注册 `JwtFilter` |
| `LoginUserDetails` | 继承 `LoginServiceImpl`；组合 `SysUser` + `effectiveLevel` + `primaryClubId` + `primaryDepartmentId`；含 Redis 可序列化 `CacheSnapshot` |
| `UserScopeResolver` | **`loadActiveRoles`**、**`isAdvisorRoleCode`**；解析 effectiveLevel、主社团/部门 scope_id |
| `DataScopeHelper` | 用户模块行级过滤：`applySysUserScope(QueryWrapper/LambdaQueryWrapper)`、`currentUser()`、`applyLevelScope(BaseQuery)` |
| `FieldMaskHelper` | 用户 DTO 脱敏：`maskSysUserDto(dto, viewer)` |
| **`LevelBasedAccess`** | **全局通用等级访问控制**（数值越小权限越高）：`checkViewable`、`checkOperable`、`checkNewLevel`、`applyLevelFilter` |
| **`UserSecurityHelper`** | **用户模块专用**：`assertUsersInScope`、`toMaskedDto`、`findInScope`、`findInScopeByUserId`、`findActiveInScope`、`assertUserEnabled`、`assertBatchNoDisabled` |
| **`UserRoleScopeHelper`** | **角色分配 scope 校验**：按 `sys_role.data_scope` 校验 `scopeType/scopeId` 组合；`assertNoDuplicateAssignment` 防重复/互斥 |
| **`UserCacheHelper`** | **Redis 登录缓存清理**：`keyForUsername` → **`user:v2:{username}`** |
| **`MenuTreeHelper`** | **菜单树内存组装**：纯静态、一次遍历建索引挂接，禁止递归查库；`buildTree`、`expandWithAncestors`、`collectPermissionCodes`、`pruneEmptyDirectories` |
| **`MenuCacheHelper`** | **菜单树 Redis 缓存**：Key `menu:tree:{userId}`（非 effectiveLevel）；`get/put/evictAll`，TTL 1 小时 |
| **`ClubSecurityHelper`** | **社团申请/合议**：指导老师、院长、校级管理员、学院范围校验 |
| **`ClubDissolveExecutor`** | **执行解散**：社团状态、部门、活动清理、角色解绑（进行中活动含 status 7） |
| **`ActivityApproverHelper`** | **活动审批**：发起人识别、社长/指导老师/学院书记/校书记查找、超时转交 |
| **`ActivityApprovalChainHelper`** | **活动审批链**：按发起人角色 + 院/校级动态生成步骤 |
| **`UserExposeHelper`** | **API 用户标识转换**：`usernameOf`、`*Username` 字段填充（社团申请/活动/审批流） |
| **`NoticeScopeHelper`** | **通知**：发起人识别、**`assertReceiverValuesValid`**（receiverType=3/4/5 须合法 JSON 数组）、接收人解析、置顶/加红 |
| **`NoticeAutoPublisher`** | **系统自动通知**：活动取消时通知参与人 |
| **`ActivitySignHelper`** | **签到**：权限、Haversine 距离、补签审批人 |
| `UserPermissionUtils` | 删除权限细粒度校验（不能删自己、仅社长可删、等级比较、同社团 scope 匹配） |

### 7.0 工具类 `SecurityUtils`（`utils` 包）

| 方法 | 说明 |
|---|---|
| `getCurrentUser()` | 从 `SecurityContext` 取 `LoginUserDetails`；未登录抛 `EIException(LOGIN_INVALID_MSG)` |
| `getCurrentLevel()` | 返回当前用户 `effectiveLevel` |

> 被 `SysRoleServiceImpl`、`LevelBasedAccess` 等模块复用，与 `DataScopeHelper.currentUser()`（可返回 null）不同，`SecurityUtils` 在业务层强制要求已登录。

### 7.1 JWT 认证与 Redis 缓存

```
登录：POST /login/allocation → AuthenticationManager → UserDetailServiceImpl
     → JwtUtil.generateToken(username) → Redis 写入 user:v2:{username}（CacheSnapshot）
请求：JwtFilter → 校验 Token → Redis 必须命中 → SecurityContext（缓存缺失则 401，不回源 DB）
注销：POST /login/logout → UserCacheHelper.evictByUsername → 删 Redis；后续同 Token 401
```

| Redis 项 | 规范 |
|---|---|
| Key | **`user:v2:{username}`**（`UserCacheHelper.keyForUsername`） |
| Value | `LoginUserDetails.CacheSnapshot`（构造参数 + 字段均 `@JsonProperty`） |
| 旧 Key | `user:{username}` 在 evict 时一并删除；命中旧 `Collection` 格式则删 Key |
| 脏数据 / 缺失 | 反序列化失败或 Key 不存在 → **401**（会话以 Redis 为准，与注销语义一致） |
| TTL | 1 小时 |
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
| `evictByUsernames(redisTemplate, usernames)` | 批量删除 **`user:v2:{username}`** 及旧版 **`user:{username}`** |

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
| **`64xx`** | **通知 + 模板** | **6401~6415** |
| **`65xx`** | **活动签到** | **6501~6519** |

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
| 9219 | `CLUB_CATEGORY_INVALID` | 社团类别无效 |
| 9220 | `CLUB_APPLY_SAVE_FAILED` | 申请保存失败 |

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

### 9.6 通知 / 模板（64xx）

| Code | 常量 | 消息 |
|---|---|---|
| 6401 | `NOTICE_NOT_FOUND` | 通知不存在 |
| 6402 | `NOTICE_CATEGORY_NOT_FOUND` | 通知分类不存在 |
| 6403 | `NOTICE_NO_PERMISSION` | 无权发送该范围的通知 |
| 6404 | `NOTICE_SCOPE_APPROVAL_REQUIRED` | 跨范围发送需额外审批 |
| 6405 | `NOTICE_STATUS_INVALID` | 通知状态不允许此操作 |
| 6406 | `NOTICE_NOT_RECEIVER` | 您不在该通知接收范围内 |
| 6407 | `NOTICE_NOT_REVOCABLE` | 该通知不可撤回 |
| 6408 | `NOTICE_PIN_EXPIRE_REQUIRED` | 置顶时必须填写置顶结束时间 |
| 6409 | `NOTICE_TEMPLATE_NOT_FOUND` | 通知模板不存在 |
| 6410 | `NOTICE_TEMPLATE_IN_USE` | 模板已被引用，已改为停用 |
| 6411 | `NOTICE_ALREADY_CONFIRMED` | 已确认阅读，无需重复操作 |
| 6412 | `TEMPLATE_CODE_TAMPER` | 模板编码与创建时间不一致 |
| 6413 | `TEMPLATE_CODE_INVALID` | 模板编码格式无效 |
| 6414 | `NOTICE_RECEIVER_VALUES_INVALID` | 接收范围值须为 JSON 数组，如 [1,2,3] |
| 6415 | `NOTICE_RECEIVER_EMPTY` | 接收范围不能为空 |

### 9.7 活动签到（65xx）

| Code | 常量 | 消息 |
|---|---|---|
| 6501 | `SIGN_CONFIG_NOT_FOUND` | 签到未配置或未启用 |
| 6502 | `SIGN_ACTIVITY_NOT_FOUND` | 活动不存在或未通过审批 |
| 6503 | `SIGN_NO_PERMISSION` | 无权操作该活动签到 |
| 6504 | `SIGN_WINDOW_CLOSED` | 不在签到时间窗口内 |
| 6505 | `SIGN_ALREADY_SIGNED` | 您已签到，不可重复签到 |
| 6506 | `SIGN_LOCATION_INVALID` | 不在签到有效范围内 |
| 6507 | `SIGN_QR_INVALID` | 扫码令牌无效 |
| 6508 | `SIGN_MODE_INVALID` | 当前活动不支持该签到方式 |
| 6509 | `SIGN_NOT_SIGNED` | 尚未签到，无法签退 |
| 6510 | `SIGN_CHECKOUT_DISABLED` | 该活动未启用签退 |
| 6511 | `SIGN_CONFLICT` | 该时间段您已参与其他活动签到 |
| 6512 | `SIGN_MAKEUP_NOT_FOUND` | 补签申请不存在 |
| 6513 | `SIGN_MAKEUP_EXPIRED` | 已超过补签申请期限 |
| 6514 | `SIGN_MAKEUP_NOT_APPROVER` | 您不是当前补签审批人 |
| 6515 | `SIGN_USER_NOT_FOUND` | 补签用户不存在 |
| 6516 | `SIGN_START_BEFORE_NOW` | 签到开始时间不能早于当前时间 |
| 6517 | `SIGN_END_TOO_LATE` | 签到结束时间不能超过开始时间后 7 天 |
| 6518 | `SIGN_CHECKOUT_BEFORE_SIGN` | 签退时间不能早于签到时间 |
| 6519 | `SIGN_CHECKOUT_TOO_LATE` | 签退时间不能超过签到时间后 7 天 |

### 9.8 认证 / 用户（节选）

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
| `deleteByUsername` / `deleteUsers` | — | 按 username 单个删除；批量按 List<Long> ids 删除；越权抛 `AccessDeniedException` |
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
| `assign(userId, roleId, scopeType, scopeId)` | ✅ 角色码校验 + 用户类型校验 | **PRESIDENT 仅限 userType=1（学生），ADMIN 仅限 userType=2（老师）**；插入 `sys_user_role` |
| `revoke(id)` | — | 根据 id 删除 `sys_user_role` 记录 |
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
| `createApply` | 超管可跳过指导老师校验；`proposedLeaderUsername`；生成 **`SQ*`** 申请编号；返回 `applicationNo` |
| `dissolveApply` | 按 **`clubCode`** 定位社团；须为 `advisor_id`；无进行中活动 |
| `pageQuery` | 支持 **`username`** 筛选申请人；列表 `UserExposeHelper` 填充 `*Username` |
| `getDetailByUsername` | 按申请人 **username** 查最近一条申请 |
| `approveCollege(ClubCollegeApproveDTO)` | Body 传 **`username` 或 `applicationNo`** + `ClubApproveDTO` |
| `approveAdmin(ClubAdminApproveDTO)` | Body 传申请人 **`username`** + 审批结果 |

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

**超时（`ActivityApprovalTimeoutTask`）：** 每小时扫描；步骤进入 >3 天日志催办；>7 天转交上级。

### 10.10 `NoticeInfoServiceImpl`（`@Slf4j`）

| 方法 | 说明 |
|---|---|
| `send` | **`assertReceiverValuesValid`**（type=3/4/5 须合法 JSON 数组）→ 范围校验 → 发布/草稿/定时 |
| `saveDraft` | 保存草稿 `status=0` |
| `publishNow` | 手动发布草稿（`doPublish` 二次校验 receiverValues） |
| `withdraw` | 撤回；`revocable=0` 不可撤 |
| `getDetail` | 详情 + 进入即已读 + 附件权限过滤 |
| `confirmRead` | 需确认通知的确认操作 |
| `myInbox` / `mySent` | 收件箱 / 发件箱 |
| `readStats` | 发布人查看已读/已确认/接收人数 |

**receiverValues 校验链路：** `assertCanSendToScope` 入口调用 **`NoticeScopeHelper.assertReceiverValuesValid`**；`parseLongList` 不再吞异常；非法如 `"abc"` 抛 **6414**，空数组 **6415**。

**定时（`NoticeScheduledTask`）：** 每分钟发布到期草稿；每分钟取消过期置顶。

### 10.11 `NoticeTemplateServiceImpl`

| 方法 | 说明 |
|---|---|
| `saveTemplate` | 自动生成 **`template_name`** 编码（`NOTICE_yyyyMMddHHmm_xxxxxx`）；返回 `templateName`；**无 id** |
| `updateTemplate` | Body 必传 **`templateName`**；校验编码与 `create_time` 分钟一致 |
| `deleteTemplate` | `?templateName=`；被引用则 `status=0` 并提示 **6410** |
| `getDetail` / `pageQuery` | 读时 **`TemplateCodeUtil.assertMatchesCreateTime`** |

### 10.12 `ActivitySignServiceImpl`（`@Slf4j`）

| 方法 | 说明 |
|---|---|
| `saveConfig` / `updateConfig` | Body **`activityNo`**；`@StartBeforeEnd` 校验窗口；开始 ≥ 当前时间；结束 ≤ 开始 +7 天 |
| `getConfig` | `?activityNo=`；直接读库返回 **`activityNo`**（`activityId` `@JsonIgnore`） |
| `sign` | `?activityNo=` 定位/扫码签到；窗口校验；半径校验；迟到标记；去重；时间冲突检测 |
| `adminSign` | `?activityNo=` 管理员手动签到（Body **`username`**） |
| `checkout` | `?activityNo=` 签退；签退 ≥ 签到时间且 ≤ 签到 +7 天；早于活动结束标记早退 |
| `applyMakeup` | 社长发起补签；活动结束+1天内 |
| `approveMakeup` | 院级指导老师 / 校级指导老师→学院书记 |
| `stats` | 应签/已签/签到率/迟到/早退/半小时分布 |
| `listRecords` | 分页签到明细（含姓名） |
| `autoCheckoutExpired` | 活动结束后自动签退 |

**定时（`ActivitySignCheckoutTask`）：** 每 5 分钟扫描已结束活动自动签退。

---

## 10A. 实体 / DTO / VO / Mapper

### DTO（`entity/dto/`）

| 类 | 字段 | 用途 |
|---|---|---|
| `AssignRoleDTO` | `userId`、`roleId`、`scopeType`、`scopeId` | `POST /sys-user-role/assign`；scope 须符合目标角色 `data_scope` |
| `ToggleStatusDTO` | `usernames`、`status`（0/1） | `PUT /sys-user/toggleStatus` |
| `AssignRoleMenuDTO` | `roleId`、`menuIds` | `POST /sys-role-menu/assign`；空列表=清空绑定 |
| `ClubCreateApplyDTO` | `clubName`、`collegeId`、`category`、`proposedLeaderUsername`、`maxMembers` | 创建申请（**无 id**） |
| `ClubDissolveApplyDTO` | **`clubCode`**、`dissolveReason` | 解散申请 |
| `ClubApproveDTO` | `approved`、`opinion` | 审批结果 |
| **`ClubCollegeApproveDTO`** | `username` 或 `applicationNo` + 继承 `ClubApproveDTO` | 学院审批 |
| **`ClubAdminApproveDTO`** | **`username`**（申请人）+ 继承 `ClubApproveDTO` | 校级审批 |
| `CouncilInitiateDTO` | `clubId`、`reason` | 合议发起 |
| **`ActivitySubmitDTO`** | `clubId`、`activityName`、`categoryId`、`activityType`、`activityLevel`、时间地点、预算、内容、附件 | `POST /activity-apply/submit` |
| **`ActivityApproveDTO`** | `version`、`opinion`、`activityLevel?` | 审批通过/驳回 |
| **`ActivityChangeDTO`** | `version`、时间地点、`changeReason` | 变更申请 |
| **`ActivityCancelDTO`** | `version`、`reason` | 取消活动 |
| **`ActivitySummaryDTO`** | `version`、`summaryContent`、`summaryAttachment` | 活动总结 |
| **`NoticeSendDTO`** | 标题、内容、分类、`receiverType`、`receiverValues`（JSON 数组字符串）等 | `POST /notice-info/send` |
| **`NoticeTemplateDTO`** | 创建无 `templateName`；更新必填 `templateName`；**无 id** | 通知模板 |
| **`SignConfigDTO`** | **`activityNo`**、签到方式、窗口（`@StartBeforeEnd`）、半径、签退、中心坐标 | `POST/PUT /activity-sign/config` |
| **`SignActionDTO`** | 定位/扫码参数、`qrToken` | `POST /activity-sign/sign?activityNo=` |
| **`AdminSignDTO`** | **`username`**、地址 | 手动签到 |
| **`MakeupApplyDTO`** / **`MakeupApproveDTO`** | 补签申请/审批 | 补签流程 |

### VO（`entity/vo/`）

| 类 | 字段 | 用途 |
|---|---|---|
| `SysUserRoleVO` | … | 用户-角色联查展示 |
| `MenuTreeVO` / `MenuTreeResultVO` | … | 菜单树 |
| `ClubApplicationDetailVO` | `application`、`currentApprover`、`queryTime` | 申请详情 |
| `CouncilSignRecordVO` | `userId`、`roleCode`、`level`、`signTime` | 合议签字 JSON 元素 |
| **`ActivityApplyDetailVO`** | `apply`、`flows`、`histories`、`currentApproverName`、`queryTime` | 活动详情 |
| **`NoticeDetailVO`** | `notice`、`highlight`、`visibleAttachments`、已读/确认、统计 | 通知详情 |
| **`SignStatsVO`** | **`activityNo`**、应签/已签/签到率/迟到/早退/时间分布 | 签到统计 |
| **`SignRecordVO`** | 姓名、方式、时间、地点、迟到/早退 | 签到明细 |

### 常量 / 工具

| 类 | 说明 |
|---|---|
| `ClubApplyConstants` | 申请类型、状态、合议状态、scope 类型、角色码常量 |
| **`ActivityApplyConstants`** | 活动状态、级别、审批流类型、发起人/审批人类型、历史状态 |
| `ClubCodeGeneratorUtil` | 申请编号 **`SQ*`**、社团编号 `{类别前缀}{时间戳}` |
| **`TemplateCodeUtil`** | 模板/业务编码 `{前缀}_{yyyyMMddHHmm}_{6位随机}` + 防篡改校验 |
| **`TemplateCodePrefix`** | `NOTICE` / `CLUB` / `ACT` |
| **`ActivityCodeGeneratorUtil`** | 生成 `ACT*` 活动编号（按分类后缀 + 防重复） |
| **`ActivityFileStorageUtil`** | 活动申请/总结附件本地存储 |
| **`NoticeConstants`** | 通知状态、接收类型、重要/紧急、来源类型 |
| **`NoticeFileStorageUtil`** | 通知附件本地存储（`uploads/notice`） |
| **`ActivitySignConstants`** | 签到方式、记录类型、补签状态；**`SIGN_WINDOW_MAX_DAYS=7`**、**`CHECKOUT_MAX_DAYS_AFTER_SIGN=7`** |

### Mapper

| 接口 / XML | 方法 | 说明 |
|---|---|---|
| `SysUserRoleMapper.selectPageWithDetail` | 分页联查 | JOIN `sys_user`、`sys_role`；按 `userIds` + `keyword` |
| `SysUserRoleMapper.selectListByUserId` | 列表 | 当前用户全部角色关联 |
| **`ActivityApplyHistoryMapper`** | — | 活动变更历史 |
| **`NoticeTemplateMapper`** | — | 通知模板 |
| **`ActivitySignConfigMapper`** | — | 签到配置 |
| **`ActivitySignMakeupMapper`** | — | 补签申请 |

---

## 11. 接口清单（重点 Controller）

> 完整路径前缀：`/Mass_Test`  
> 统一响应：`R { code, msg, data? }`  
> 认证 Header：`Token: <jwt>`

### 11.1 认证模块

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/login/allocation` | POST | `@IgnoreAuth` | `name` + `password` → `{ token, username }` |
| `/login/logout` | POST | `STUDENT (4)` | 删 Redis `user:v2:{username}`；返回「已退出」；幂等 |
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

### 11.3 `SysUserRoleController`（已统一为 4 接口）

根路径：`/sys-user-role`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| **`/assign`** | POST | `ADMIN (1)` | 分配角色；Query 参数：`userId`、`roleId`、`scopeType`、`scopeId`；**PRESIDENT 仅限 userType=1（学生），ADMIN 仅限 userType=2（老师）** |
| **`/revoke`** | DELETE | `ADMIN (1)` | 撤销角色关联；Query 参数：`id` |
| **`/list`** | GET | `ADMIN (1)` | 分页联查；支持 `keyword`；按管理员数据范围过滤 |
| **`/my-roles`** | GET | `STUDENT (4)` | 当前登录用户角色列表 |

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

### 11.5 `SysMenuController`（已统一为 4 接口）

根路径：`/sys-menu`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/tree` | GET | `STUDENT (4)` | 当前用户菜单树 + `permissions`；Redis `menu:tree:{userId}` |
| `/list` | GET | `ADMIN (1)` | 全量分页；`menuName` 模糊 + `menuType` 过滤 |
| `/save` | POST | `ADMIN (1)` | 新增/更新；校验父级、循环、同级名、类型字段 |
| `/delete/{id}` | DELETE | `ADMIN (1)` | 无子菜单且可操作时删除；清理 `sys_role_menu` |

### 11.6 `SysRoleMenuController`（已统一为 2 接口）

根路径：`/sys-role-menu`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/assign` | POST | `ADMIN (1)` | 全量覆盖分配；Body: `AssignRoleMenuDTO`；先删后增 |
| `/listByRole/{roleId}` | GET | `ADMIN (1)` | 返回已绑定 `menu_id` 列表 |

### 11.7 `ClubApplicationController`（9 接口）

根路径：`/club-application`（规范见 `.cursor/skills/活动.md`、`.cursor/skills/id.md`）

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/categories` | GET | `STUDENT (4)` | 固定六类社团性质 |
| `/apply/create` | POST | `ADMIN (1)` | 返回 **`applicationNo`**（`SQ*`） |
| `/apply/dissolve` | POST | `ADMIN (1)` | Body **`clubCode`** |
| `/apply/list` | GET | `ADMIN (1)` | `?username=` 筛选申请人 |
| `/apply/detail` | GET | `ADMIN (1)` | **`?username=`** 查申请人最近申请（不用 id） |
| `/approve/college` | POST | `ADMIN (1)` | Body: **`ClubCollegeApproveDTO`** |
| `/approve/admin` | POST | `ADMIN (1)` | Body: **`ClubAdminApproveDTO`**（含申请人 username） |

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

### 11.10 `NoticeInfoController`（10 接口）

根路径：`/notice-info`（规范见 `.cursor/skills/通知.md`）

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/send` | POST | `ADMIN (1)` | 发送/定时/草稿 |
| `/draft` | POST | `ADMIN (1)` | 保存草稿 |
| `/publish/{id}` | POST | `ADMIN (1)` | 手动发布草稿 |
| `/withdraw/{id}` | POST | `ADMIN (1)` | 撤回通知 |
| `/detail/{id}` | GET | `STUDENT (4)` | 详情（进入即已读） |
| `/confirm/{id}` | POST | `STUDENT (4)` | 确认阅读 |
| `/inbox` | GET | `STUDENT (4)` | 我的收件箱 |
| `/sent` | GET | `ADMIN (1)` | 我发送的通知 |
| `/stats/{id}` | GET | `ADMIN (1)` | 已读/确认统计 |
| `/upload` | POST | `ADMIN (1)` | 上传附件 |

### 11.11 `NoticeTemplateController`（5 接口）

根路径：`/notice-template`

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/save` | POST | `ADMIN (1)` | 无需传 `templateName`；返回生成的编码 |
| `/update` | PUT | `ADMIN (1)` | Body 必传 **`templateName`** |
| `/delete` | DELETE | `ADMIN (1)` | **`?templateName=`**（被引用则停用） |
| `/detail` | GET | `ADMIN (1)` | **`?templateName=`** |
| `/list` | GET | `ADMIN (1)` | 分页列表（响应无 id） |

### 11.12 `ActivitySignController`（10 接口）

根路径：`/activity-sign`（规范见 `.cursor/skills/签到.md`）

| 路径 | 方法 | `@RequiresLevel` | 说明 |
|---|---|---|---|
| `/config` | POST | `ADMIN (1)` | Body **`activityNo`**；配置签到 |
| `/config` | PUT | `ADMIN (1)` | Body **`activityNo`**；更新配置 |
| `/config` | GET | `STUDENT (4)` | **`?activityNo=`** 查询配置 |
| `/sign` | POST | `STUDENT (4)` | **`?activityNo=`** 定位/扫码签到 |
| `/admin/sign` | POST | `ADMIN (1)` | **`?activityNo=`** 手动签到 |
| `/checkout` | POST | `STUDENT (4)` | **`?activityNo=`** 签退 |
| `/apply` | POST | `ADMIN (1)` | **`?activityNo=`** 社长发起补签 |
| `/approve/{applyId}` | POST | `ADMIN (1)` | 补签审批 |
| `/stats` | GET | `ADMIN (1)` | **`?activityNo=`** 签到统计 |
| `/list` | GET | `ADMIN (1)` | **`?activityNo=`** 签到明细 |

### 11.13 其他 Controller 根路径

| Controller | 根路径 | 说明 |
|---|---|---|
| `LoginController` | `/login` | 登录（见 11.1） |
| `RegisterController` | `/register` | 注册（见 11.1） |
| `NotificationGrantController` | — | 空类，预留扩展 |

---

## 12. 前端结构（`club-admin-frontend`）

```
src/api/crudFactory.ts    # 通用 CRUD 工厂
src/utils/request.ts      # baseURL=/Mass_Test，Header Token 注入
src/stores/user.ts        # token、username、角色
src/router/dynamic.ts     # 后端菜单驱动动态路由
```

**开发服务器：** 端口 `5173`，代理 `/Mass_Test` → `http://localhost:100`

---

## 13. 关键业务流程

### 13.1 登录 → 权限缓存 → 业务请求

```
login → JWT(subject=username) → Redis user:v2:{username}（CacheSnapshot）
     → 业务请求携带 Token → JwtFilter 加载 LoginUserDetails
     → @RequiresLevel 准入 → Service 数据过滤（DataScopeHelper / LevelBasedAccess）+ 字段脱敏（UserSecurityHelper）
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
创建：POST /apply/create → proposedLeaderUsername → SQ* 编号 → 待学院审批
      → POST /approve/college（Body: username 或 applicationNo）
      → POST /approve/admin（Body: 申请人 username）→ 激活社团 + CLUB_PRESIDENT

详情/列表：GET /apply/detail?username=1001（username 为申请人学号/工号，非主键 id）

解散：POST /apply/dissolve（clubCode）→ 审批链同上
```

### 13.6 通知

```
发送：POST /notice-info/send
      → assertReceiverValuesValid（type=3/4/5：receiverValues 须 "[1,2,3]" 格式，非法抛 6414）
      → assertCanSendToScope（ADMIN 也先过 receiverValues 校验）
      → doPublish 发布前二次校验

模板：POST /notice-template/save → 返回 templateName（存 template_name 列）
      → GET/PUT/DELETE 均用 ?templateName= 或 Body templateName，不暴露 id
```

### 13.7 活动签到

```
配置：POST/PUT /activity-sign/config → Body activityNo、sign_mode、窗口、半径、签退
      → @StartBeforeEnd：signStartTime < signEndTime
      → signStartTime ≥ 当前时间；signEndTime ≤ signStartTime + 7 天（6516/6517）
      → 扫码模式生成 qrToken；响应 activityNo（不暴露 activityId）

签到：POST /sign?activityNo= → 窗口校验 → 定位(Haversine)/扫码 → 迟到标记 → uk_activity_user 去重
      → 同时段其他活动冲突拒绝(6511)

签退：POST /checkout?activityNo= → 启用签退时；签退时间 ≥ 签到时间且 ≤ 签到 +7 天（6518/6519）
      → 早于活动结束时间标记早退
      → ActivitySignCheckoutTask 每5分钟：活动结束后自动签退（时间钳制在 [签到, 签到+7天]）

补签：POST /apply?activityNo=（社长）→ 活动结束+1天内
      → POST /approve/{applyId}：院级指导老师 / 校级指导老师→学院书记
      → 通过写入 sign_type=3

统计：GET /stats?activityNo=、GET /list?activityNo=
```

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
        SV --> AAH[ActivityApproverHelper]
        SV --> AAC[ActivityApprovalChainHelper]
        SV --> ATT[ActivityApprovalTimeoutTask]
        SV --> NSH[NoticeScopeHelper]
        SV --> NAP[NoticeAutoPublisher]
        SV --> NST[NoticeScheduledTask]
        SV --> ASH[ActivitySignHelper]
        SV --> SCT[ActivitySignCheckoutTask]
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
| ✅ 已完成 | **活动审批模块** | `ActivityApplyController` 10 接口；见 `活动审批模块.md` |
| ✅ 已完成 | **通知模块** | `NoticeInfoController` + `NoticeTemplateController`；见 `通知.md` |
| ✅ 已完成 | **活动签到模块** | `ActivitySignController` 10 接口；**`activityNo`** 对外；时间校验见 `签到.md` |
| ✅ 已修复 | 签到 DTO/接口暴露 activityId | 改为 **`activityNo`** + 签退/配置时间校验（6516~6519） |
| ✅ 已修复 | Redis CacheSnapshot 反序列化 | `@JsonProperty` 全字段；Key 升级 **`user:v2:{username}`**；脏数据删 Key |
| ✅ 已修复 | 通知 receiverValues 静默失败 | **`assertReceiverValuesValid`**；6414/6415 |
| ✅ 已完成 | 通知模板编码 | **`template_name` 存编码**；`TemplateCodeUtil`；6412/6413 防篡改 |
| ✅ 已完成 | username 对外暴露 | 社团申请/用户模块；见 **`id.md`** |
| ✅ 已清理 | 无引用空壳 Service | 删除 9 组仅 MyBatis-Plus 脚手架 Service（Mapper 仍保留） |
| ⏳ 待做 | 种子角色 `CLUB_PRESIDENT` | 创建申请校级通过后绑定社长依赖该角色 |
| ⏳ 待做 | 通知跨范围审批 | 当前跨范围发送返回 **6404**，完整审批流待实现 |
| ⏳ 待做 | 种子密码明文 | BCrypt 迁移或重新注册 |
| ⏳ 待做 | entity / model 重复 | 统一实体包 |
| ⏳ 待做 | Fastjson 1.2.83 安全风险 | 升级或替换 Jackson |

---

## 16. 相关文档

| 文档 | 路径 | 说明 |
|---|---|---|
| **项目概览（本文档）** | `.cursor/skills/PROJECT_OVERVIEW.md` | 结构与接口说明 |
| 更新扫描清单 | `.cursor/skills/更新信息.md` | 本文档更新依据 |
| **username 暴露规范** | `.cursor/skills/id.md` | 对外 username、对内 userId |
| JWT 登录规范 | `.cursor/skills/login.md` | Redis Key `user:v2:`、subject 规范 |
| 权限过滤方案 | `.cursor/skills/权限过滤.md` | 五级可见范围 |
| **异常码规范** | `.cursor/skills/Erroconfig.md` | 编码区间与常量命名 |
| **社团申请/合议** | `.cursor/skills/活动.md` | 创建/解散/审批/合议业务规范 |
| **活动审批** | `.cursor/skills/活动审批模块.md` | 活动申请/审批/变更/取消/总结/超时 |
| **通知** | `.cursor/skills/通知.md` | 通知发送/撤回/已读/模板/定时 |
| **活动签到** | `.cursor/skills/签到.md` | 签到配置/执行/补签/统计 |
| 前端开发规范 | `.cursor/skills/skill.md` | Vue3/Element Plus |
| OpenAPI JSON | `src/api/spec/api-docs.json` | 机器可读 API |
| 数据库脚本 | `mysql/mass_test1.sql` | 建表 + 种子 |
| 活动审批 DDL | `mysql/activity_approval_migration.sql` | 活动模块增量 |
| **通知 DDL** | `mysql/notice_module_migration.sql` | 通知模块增量 |
| **签到 DDL** | `mysql/activity_sign_migration.sql` | 签到配置/补签/签到表扩展 |

---

## 17. 近期变更文件索引

> RBAC、社团生命周期、活动审批模块核心变更，按包路径归类。

### 17.0C 2026-07-16 username / Redis / 通知 / 社团

| 包 / 文件 | 变更摘要 |
|---|---|
| **`security/LoginUserDetails.java`** | `CacheSnapshot` 全字段 `@JsonProperty` + `@Getter` |
| **`security/UserCacheHelper.java`** | Redis Key **`user:v2:{username}`**；evict 含旧 Key |
| **`filter/JwtFilter.java`** | Redis 会话必存在；缺失/脏数据 401；不回源 DB |
| **`controller/LoginController.java`** | 登录写 `CacheSnapshot`；**`POST /logout`** 删会话缓存 |
| **`security/UserExposeHelper.java`** | 社团/活动/通知用户 FK → `*Username` |
| **`controller/ClubApplicationController.java`** | username 查询/审批；无路径 id |
| **`entity/dto/ClubCreateApplyDTO.java`** | `proposedLeaderUsername`；`UsernameStringDeserializer` |
| **`utils/ClubCodeGeneratorUtil.java`** | 申请编号 **`SQ*`** |
| **`utils/TemplateCodeUtil.java`** | 模板编码生成 + 分钟级防篡改 |
| **`entity/constants/TemplateCodePrefix.java`** | `NOTICE` / `CLUB` / `ACT` |
| **`service/impl/NoticeTemplateServiceImpl.java`** | `template_name` 存编码；API 不暴露 id |
| **`security/NoticeScopeHelper.java`** | **`assertReceiverValuesValid`**；`parseLongList` 严格化 |
| **`exception/ErrorConfig.java`** | **6412~6415**；签到段改为 **65xx** |
| **删除无引用 Service×9** | `CommonService`、`SysClubService`、`ActivityApproveFlowService` 等空壳 |

### 17.0E 2026-07-01 角色分配校验 / 用户删除权限 / username 重构

| 包 / 文件 | 变更摘要 |
|---|---|
| **`security/UserPermissionUtils.java`** | **新增**：`@Component` 注入，`checkDeletePermission` / `canDelete` 两条方法；规则：不能删自己、仅社长可删、等级高于目标、同社团 scope 匹配 |
| **`controller/SysUserRoleController.java`** | 新增 `POST /assign`（Query 参数：userId/roleId/scopeType/scopeId）+ `DELETE /revoke`（Query 参数：id）；均加 `@RequiresLevel(minLevel = Level.ADMIN)` |
| **`service/SysUserRoleService.java`** | 新增 `assign(userId, roleId, scopeType, scopeId)`、`revoke(id)` |
| **`service/impl/SysUserRoleServiceImpl.java`** | `assign`：校验 PRESIDENT 仅限 userType=1、ADMIN 仅限 userType=2；`revoke`：按 id 删除 |
| **`controller/SysUserController.java`** | 修复 `GET /detailSysUser/{username}` 路径（原为损坏字符串）；新增 `DELETE /deleteSysUser/{username}`；`addSysUser` 中 `getDetail` 改用 `username` |
| **`service/SysUserService.java`** | `getDetail(String name)` → `getDetail(String username)`；新增 `deleteByUsername(String username)` |
| **`service/impl/SysUserServiceImpl.java`** | `getDetail` 参数重命名；新增 `deleteByUsername` 实现（调用 `baseMapper.deleteByUsername`） |
| **`mapper/SysUserMapper.java`** | 新增 `deleteByUsername(@Param("username") String username)` |
| **`mapper/SysUserMapper.xml`** | 新增 `<delete id="deleteByUsername">` SQL |

### 17.0D 2026-07-16 活动签到 activityNo 与时间校验

| 包 / 文件 | 变更摘要 |
|---|---|
| **`controller/ActivitySignController.java`** | 路径参数 `{activityId}` → **`?activityNo=`** |
| **`entity/dto/SignConfigDTO.java`** | **`activityNo`** + **`@StartBeforeEnd`** |
| **`entity/ActivitySignConfig.java`** | `@JsonIgnore activityId`；`activityNo` 为 `@TableField(exist=false)` 瞬态字段，查询时从 `ActivityApply` 反查附加 |
| **`entity/vo/SignStatsVO.java`** | **`activityNo`** 替代 `activityId` |
| **`service/impl/ActivitySignServiceImpl.java`** | `enrichConfigActivityNo` 反查附加；`requireApprovedActivityByNo`；配置/签退时间校验 |
| **`mysql/activity_sign_migration.sql`** | 通过 `activity_id` 关联活动，`activityNo` 不冗余存储 |
| **`entity/constants/ActivitySignConstants.java`** | `SIGN_WINDOW_MAX_DAYS`、`CHECKOUT_MAX_DAYS_AFTER_SIGN` |
| **`exception/ErrorConfig.java`** | **6516~6519** |

### 17.0 2026-07-05 活动审批模块

| 包 / 文件 | 变更摘要 |
|---|---|
| **`controller/ActivityApplyController.java`** | 10 接口 |
| **`service/impl/ActivityApplyServiceImpl.java`** | 活动审批全流程 |
| **`security/ActivityApproverHelper.java`** 等 | 审批链与审批人 |
| **`task/ActivityApprovalTimeoutTask.java`** | 超时催办/转交 |
| **`mysql/activity_approval_migration.sql`** | 增量 DDL |

### 17.0A 2026-07-05 通知模块

| 包 / 文件 | 变更摘要 |
|---|---|
| **`controller/NoticeInfoController.java`** | 10 接口：send/draft/publish/withdraw/detail/confirm/inbox/sent/stats/upload |
| **`controller/NoticeTemplateController.java`** | 5 接口：模板 CRUD |
| **`service/impl/NoticeInfoServiceImpl.java`** | 发送/撤回/已读/确认/收件箱；`@Slf4j` |
| **`service/impl/NoticeTemplateServiceImpl.java`** | 模板管理 |
| **`service/NoticeAutoPublisher.java`** | 活动取消自动通知 |
| **`security/NoticeScopeHelper.java`** | 范围校验、接收人解析、置顶规则 |
| **`task/NoticeScheduledTask.java`** | 定时发送 + 置顶过期 |
| **`entity/NoticeInfo.java`** | 扩展置顶、长期可见、附件等字段 |
| **`entity/NoticeTemplate.java`** | 通知模板实体 |
| **`entity/constants/NoticeConstants.java`** | 状态/类型常量 |
| **`entity/dto/NoticeSendDTO.java`** 等 | 发送/模板 DTO |
| **`entity/vo/NoticeDetailVO.java`** | 详情 VO |
| **`utils/NoticeFileStorageUtil.java`** | 附件存储 |
| **`exception/ErrorConfig.java`** | **`64xx`** 通知错误码（含 6412~6415） |
| **`mysql/notice_module_migration.sql`** | 增量 DDL |
| **`ActivityApplyServiceImpl.java`** | 取消时调用 `NoticeAutoPublisher` |

### 17.0B 2026-07-05 活动签到模块

| 包 / 文件 | 变更摘要 |
|---|---|
| **`controller/ActivitySignController.java`** | 10 接口（config/sign/checkout/makeup/stats/list） |
| **`service/impl/ActivitySignServiceImpl.java`** | 签到全流程；`@Slf4j`；`@Transactional` |
| **`security/ActivitySignHelper.java`** | 权限、距离计算、补签审批人 |
| **`task/ActivitySignCheckoutTask.java`** | 自动签退定时任务 |
| **`entity/ActivitySignConfig.java`** | 签到配置 |
| **`entity/ActivitySignMakeup.java`** | 补签申请 |
| **`entity/ActivitySign.java`** | 扩展 lat/lng、迟到/早退、签退时间 |
| **`entity/constants/ActivitySignConstants.java`** | 常量 |
| **`entity/dto/Sign*.java`**、`Makeup*.java` | DTO |
| **`entity/vo/SignStatsVO.java`**、`SignRecordVO.java` | 统计/明细 VO |
| **`exception/ErrorConfig.java`** | **`65xx`** 签到 |
| **`mysql/activity_sign_migration.sql`** | 增量 DDL |

### 17.1 2026-07-04 RBAC / 社团模块 — Controller

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
| **`entity/ActivityApply.java`** | 活动申请实体（含乐观锁与新字段） |

*文档结束*
