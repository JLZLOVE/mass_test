# Mass_Test RBAC 权限过滤说明书

> 文档：`.cursor/skills/role.md`  
> 与代码 / `PROJECT_OVERVIEW.md` §3.2、§7 同步（2026-07-18）  
> 旧任务稿 `role分配.md` 已废弃，以本文为准。

---

## 1. 目标与模型

本项目权限 = **角色（Role）+ 等级（Level）+ 数据范围（Scope）+ 菜单（Menu）**，在 Service / AOP 层完成，不依赖前端藏按钮。

```
sys_user
   ↕ sys_user_role（scope_type / scope_id）
sys_role（role_code、role_level、data_scope）
   ↕ sys_role_menu
sys_menu
```

登录后解析为 `LoginUserDetails`：

| 字段 | 含义 |
|---|---|
| `effectiveLevel` | 多角色取最高权限（**数值最小**） |
| `primaryClubId` | 社长主社团（scope_type=2） |
| `primaryDepartmentId` | 部长主部门（scope_type=3） |

---

## 2. 三层分离（必须遵守）

| 层级 | 职责 | 实现 | 禁止 |
|---|---|---|---|
| **① 准入鉴权** | 谁可以调这个接口 | `@RequiresLevel` + `LevelAspect` | 在切面里拼 SQL / 脱敏 |
| **② 数据行过滤** | 能看见哪些行 | `DataScopeHelper`、`LevelBasedAccess`、各业务 Helper | 只靠前端筛选 |
| **③ 字段脱敏** | 行内敏感字段 | `FieldMaskHelper` + `UserSecurityHelper.toMaskedDto` | 把脱敏写进 Controller |

同一业务只保留**一套** Controller 接口；不按等级拆 `_F/_B`。

---

## 3. 等级常量（`Level`）

**约定：数值越小权限越高。**

| 常量 | 值 | 身份 | 用户模块可见范围（`DataScopeHelper.applySysUserScope`） |
|---|---|---|---|
| `SUPER_ADMIN` | 0 | 超管 | 全部 |
| `ADMIN` | 1 | 管理员/导员/指导老师档 | 仅学生（`user_type=1`） |
| `CLUB_LEADER` | 2 | 社长 | 本社团成员 |
| `DEPT_LEADER` | 3 | 部长 | 本部门成员 |
| `STUDENT` | 4 | 普通学生 | 仅自己 |

`effectiveLevel` 由 `UserScopeResolver` 解析：

1. 优先 `role_code` 映射表（`SUPER_ADMIN`→0 … `MEMBER`→4）  
2. `ADVISOR*` 前缀 → 1  
3. 否则回退 `sys_role.role_level`  
4. 多角色取 **min(level)**

工具方法：`UserScopeResolver.loadActiveRoles`、`isAdvisorRoleCode`。

---

## 4. 准入鉴权：`@RequiresLevel`

```java
@RequiresLevel(minLevel = Level.ADMIN)  // effectiveLevel ≤ 1 可访问
```

- 判定：`currentEffectiveLevel <= minLevel`  
- 失败：抛权限不足（由全局异常处理）  
- 类级 / 方法级均可；方法优先  

**示例**

| 接口场景 | 常用 `minLevel` |
|---|---|
| 登录后基础读 | `STUDENT (4)` |
| 管理端写操作 | `ADMIN (1)` |
| 超管合议发起 | `SUPER_ADMIN (0)` |

免登录另用 `@IgnoreAuth`（与等级无关）。

---

## 5. 数据范围：角色分配与查询过滤

### 5.1 角色上的 `data_scope`（`sys_role`）

分配用户角色时，由 `UserRoleScopeHelper.validateScope` 按 **角色的 data_scope**（不是写死 role_code）校验：

| data_scope | 含义 | 分配时 scopeType / scopeId |
|---|---|---|
| 0 | 全部 | 均为 **null** |
| 1 | 本学院 | scopeType=**1** + 学院 id |
| 2 | 本社团 | scopeType=**2** + 社团 id |
| 3 | 本部门 | scopeType=**3** + 部门 id |
| 4 | 仅自己 | 均为 **null** |

另有 `assertNoDuplicateAssignment`：精确重复拒绝；全局范围与特定范围互斥。

### 5.2 用户列表行过滤（`DataScopeHelper`）

在用户分页 / 联查前调用 `applySysUserScope`，按 §3 表格拼条件。  
社团 / 活动 / 通知等模块另有专用 Helper（如 `ClubSecurityHelper`、`NoticeScopeHelper`），规则见各业务文档。

### 5.3 等级互斥操作（`LevelBasedAccess`）

| 方法 | 用途 |
|---|---|
| `checkViewable` | 能否查看目标等级资源 |
| `checkOperable` | 能否改删（不能动更高权限） |
| `checkNewLevel` | 能否创建/提升到某等级 |
| `applyLevelFilter` | Wrapper 追加 `ge(levelColumn, current)` |

---

## 6. 菜单权限（RBAC 功能点）

| 组件 | 作用 |
|---|---|
| `sys_role_menu` | 角色绑定菜单 |
| `MenuTreeHelper` | 内存组树、补祖先、收权限码 |
| `MenuCacheHelper` | Redis `menu:tree:{userId}`，变更后 `evictAll` |

菜单可见性 ≠ 数据行范围：有菜单仍可能只看见本社团数据。

---

## 7. 角色分配接口（现行）

根路径：`/sys-user-role`（需 `ADMIN`）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/assign` | 分配；校验 data_scope + 防重复；成功后清用户 Redis 会话（如有） |
| DELETE | `/revoke` | 按关联 id 撤销 |
| GET | `/list` | 分页联查（带数据范围） |
| GET | `/my-roles` | 当前用户角色 |

对外用户标识优先 **username**（见暴露规范）；内部表仍用 `user_id`。

**🔴 ID 对外暴露禁令（2026-07-24）**：分配角色时前端传 `clubName`（社团名称），服务端 `resolveClubName()` 内部转为 `scopeId`。响应 VO 不暴露 `id`/`roleId`/`scopeId`，改为 `clubName`/`collegeName`/`departmentName`。详见 `skill.md` 核心铁律章节。

分配业务规则补充：

- 社长类角色通常仅学生账号  
- 禁用用户不可分配 / 登录（`USER_DISABLED`）  
- 不能随意撤销自己正在使用的关键角色（见 `ErrorConfig` 9xxx）

---

## 8. 请求链路（端到端）

```
Token → JwtFilter（Redis user:v2:{username}）
     → LoginUserDetails 进入 SecurityContext
     → @RequiresLevel（LevelAspect）
     → Service：DataScopeHelper / LevelBasedAccess / 业务 Helper
     → 可选 FieldMaskHelper 脱敏
     → R 响应
```

角色或菜单变更后：

1. 清菜单缓存 `MenuCacheHelper.evictAll`  
2. 视情况 `UserCacheHelper.evictByUsername`，迫使重新登录加载新等级/scope  

---

## 9. 与其它文档的分工

| 文档 | 内容 |
|---|---|
| **本文 `role.md`** | RBAC + 等级 + 数据范围总说明书 |
| `权限过滤.md` | 早期五级方案草稿（设计演进参考） |
| `login.md` | JWT / Redis 会话 / 注销 |
| `活动.md` / `通知.md` / `签到.md` | 各业务内额外鉴权 |
| `Erroconfig.md` | 9xxx 角色、9012 scope 非法等 |

---

## 10. 实现检查清单

- [ ] 新接口是否加了正确的 `@RequiresLevel`  
- [ ] 列表查询是否走了对应 Scope / Helper  
- [ ] 角色分配是否按 `data_scope` 校验而非硬编码 role_code  
- [ ] 变更角色/菜单后是否清 Redis  
- [ ] 响应是否避免把内部 userId 当对外主键暴露  
