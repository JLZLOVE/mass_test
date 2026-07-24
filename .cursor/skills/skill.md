---
name: club-admin-frontend
description: 社团综合管理平台前端开发技能。基于 Vue3 + TypeScript + Element Plus + Vite，实现社团/活动/审批/通知/统计/用户权限模块。社团管理以 .cursor/skills/社团权限过滤加强.md 为设计真源；API 对齐当前 Spring 控制器（非旧版 _F/_B 命名）。
---

# 社团管理平台前端技能（Cursor 专用）

## 技术栈（必须遵守）
- Vue 3 (Composition API + `<script setup>`)
- TypeScript
- Vue Router (Hash 模式)
- Pinia (状态管理)
- Element Plus (按需导入)
- Axios (请求封装)
- ECharts (图表)
- @vueuse/core

## 项目结构（必须生成）
```
club-admin-frontend/
├── src/
│   ├── api/               # 接口封装（按后端路径命名）
│   ├── assets/
│   ├── components/
│   ├── composables/       # 如 useMemberCount
│   ├── layouts/           # 左侧菜单 + 右侧内容 + 顶部栏
│   ├── router/
│   ├── stores/            # user / menu
│   ├── types/             # generated.ts + api.ts（R / PageResult）
│   ├── utils/             # request、level、permission、format
│   ├── views/
│   │   ├── login/
│   │   ├── club/          # list / detail / council
│   │   ├── activity/      # apply / approve-flow / sign
│   │   ├── member/
│   │   ├── notice/
│   │   ├── profile/
│   │   ├── dashboard/
│   │   └── statistics/
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## API 集成规则

### 1. 请求客户端 (`src/utils/request.ts`)
- 基础 URL: `VITE_API_BASE_URL` 或默认 `/Mass_Test`
- 请求头：`Authorization: Bearer ${token}` 与 `Token`
- 响应成功：`code === 0`（或 `code` 未定义）；否则 `ElMessage.error`，`401` 跳转登录
- 登录：按 `登录设计.md` / 现有 `api/login` 实现（勿再假设旧 OpenAPI 的 `_F` 命名）

### 2. 分页约定
统一使用后端 `MPUtil.getPage` 约定：
- 请求：`page`、`limit`（不是 `pageNum` / `pageSize`）
- 响应：`R.data` 为 MyBatis-Plus 分页 → `{ records, total, size, current, pages }`

### 3. 主要接口路径（当前后端，禁止再写 listXxx_F）

| 模块 | 路径前缀 | 说明 |
|---|---|---|
| 社团管理 | `/sys-club/*` | `list` / `detail/{clubCode}` / `departments` / `members` |
| 学院 | `/sys-college/list` | 远程搜索，按权限过滤 |
| 社团申请 | `/club-application/*` | `categories`、`apply/create`、`apply/dissolve`、审批 |
| 社团合议 | `/club-council/*` | `list`、`detail`、`council/initiate`、`council/sign/{id}` |
| 成员数聚合 | `/club-statistics/list?clubIds=` | `{ clubId, memberCount }[]` |
| 活动 | `/activity-apply/*`、`/activity-sign/*` | 见活动相关 skill |
| 用户/角色 | `/sys-user/*`、`/sys-user-role/*` | 成员管理 |
| 通知 | `/notice-info/*` | |
| 菜单 | `/sys-menu/*` | 菜单树 + permissions |
| 门户 | `/portal/*` | 公开数据，管理端勿作主数据源 |

### 4. TypeScript 类型
- `src/types/api.ts`：`R<T>`
- `src/types/generated.ts`：业务类型（含 `SysClubItem`、`ClubCategoryItem`、`ClubMemberCount`、`ClubCouncilDetail` 等）
- 成功字段以实际拦截器为准：`code` + `msg` + `data`

---

## 业务模块详细需求

### 登录认证
- 页面：`/login`
- 成功后存 `token` / `username`，`loadUserProfile` 拉用户详情与角色，计算 `effectiveLevel`、`clubScopeIds`
- 跳转工作台，动态菜单 + `STATIC_ROUTES` 补齐

### 社团管理（设计真源：`.cursor/skills/社团权限过滤加强.md`）

> 核心原则：**菜单隔离、流程分离、行权下沉、数据聚合解耦**。Level 4 **无菜单入口**。**部长（Level 3）≠ 社长（Level 2），权限域严格分离。**

#### 用户分层与准入
| Level | 角色 | 菜单可见 | 数据范围 | 操作列 | Tab 可见性 |
|---|---|---|---|---|---|
| 0/1 | 超管/管理员 | ✅ | 全量社团（含已解散） | 查看详情 + 解散申请 + 合议签字 | 全部三个 Tab |
| 2 | 社长 | ✅ | 本人管辖社团（`scope_type=2`） | 查看详情 + 解散申请 + 合议签字 | 全部三个 Tab |
| 3 | 部长 | ✅（`minLevel: 3`） | 本部门挂靠社团（`scope_type=3`） | **仅「查看详情」** | **仅「正常社团」Tab** |
| 4 | 学生 | ❌ | — | — | — |

> **部长（Level 3）进入社团管理页面的唯一业务目的**：只读查看本部门挂靠社团基本信息，作为跳转至「成员管理」和「活动申请」的路由锚点。**不是社团管理者。**

#### 部长进入社团列表页的特殊交互（来自 `社团权限过滤加强.md` §1.3）
- 顶部显示静默提示条（`el-alert` 类型 `info`，可关闭）：
  > "当前为只读视图。如需管理本部门成员，请前往「成员管理」；如需发起活动，请前往「活动申请」。"
- 仅显示「正常社团」Tab，「申请解散中」和「合议中」Tab 完全隐藏
- 操作列仅显示「查看详情」按钮，禁止灰色禁用按钮

#### 列表页 `/club`（`views/club/list.vue`，`keep-alive` 名 `ClubList`）
- **筛选**：类别（`GET /club-application/categories` → `{code,label}`，禁止硬编码）、学院远程搜索、状态（正常/已解散，与 Tab 联动重置）、关键词防抖 300ms（名称/编号）
- **Tab 物理隔离**：
  - `normal`：正常运营（排除进行中解散申请与合议）
  - `dissolving`：解散申请中（`apply_type=2` 且 `status∈{1,2}`）— **Level 3 不可见**
  - `council`：合议中（`club_council.status=1`）— **Level 3 不可见**
- **列**：编号（等宽）、名称（类别 2px 色块 + 跳转详情）、类别、指导老师（空→「待指派」）、成员数（`useMemberCount` 批量聚合）、状态圆点、操作列
- **操作列**（可见即有权，**禁止灰色禁用按钮**，按钮显隐由后端下发的布尔字段驱动）：
  - `canViewDetail` → 查看详情：`effectiveLevel ≤ 3` → `/club/detail/{clubCode}?from=club-list`
  - `canDissolve` → 解散申请：仅 Level 0/1/2，二次确认（输入社团名）+ 原因 → `POST /club-application/apply/dissolve` → 切 Tab
  - `canSignCouncil` → 合议签字：仅 Level 0/1，`/club/council/{clubId}`
- **创建社团**：仅 Level ≤ 1，`POST /club-application/apply/create`
- **成员数**：`composables/useMemberCount.ts`，`GET /club-statistics/list?clubIds=`，TTL 5s；失败显示 `--` + 行级重试；加载用骨架，勿提前显示 `0人`

#### 详情页 `/club/detail/:clubCode`（社长/部长差异化，来自 `社团权限过滤加强.md` §5）
- **社长（Level 2）进入**：展示社团信息卡、部门架构树、全社团成员列表（分页）、历史活动时间线。操作按钮：解散申请、合议签字（如满足条件）、编辑社团信息。
- **部长（Level 3）进入**：展示社团信息卡（不含编辑入口）、历史活动时间线。成员列表**仅显示本部门成员**（后端 `scope_type=3` 自动过滤）。顶部提示条："当前为只读视图，如需管理成员请前往成员管理。" **操作按钮全部隐藏**（无解散、无合议、无编辑）。
- 无权限：提示并回退 `/club`

#### 合议页 `/club/council/:clubId`
- `GET /club-council/detail?clubId=`；签字 `POST /club-council/council/sign/{id}`
- 展示签字人列表与 `canSign` / `alreadySigned`

#### 关键前端文件
- `api/sysClub.ts`、`clubApplication.ts`、`sysCollege.ts`、`clubStatistics.ts`、`clubCouncil.ts`
- `views/club/list.vue`、`detail.vue`、`council.vue`
- `stores/menu.ts`：`club` minLevel **3**；子路由 `club/detail/:clubCode`、`club/council/:clubId`

### 活动管理
#### 活动申请 / 活动管理 (`/activity/apply`)
- 列表：`ActivityApply`，搜索活动名称、状态
- 状态：草稿(1)、待审批(2)、审批中(3)、已通过(4)、已驳回(5)、已取消(6)…
- Level 4 可访问列表/详情，数据范围仅本人参与活动（后端过滤）

#### 审批流程 (`/activity/approve-flow/:applyId`)
- `el-steps` 展示审批步骤；当前审批人可同意/驳回

#### 签到 (`/activity/sign/:activityId?`)
- 签到管理 minLevel: 2；学生侧签到/签退走活动详情或工作台能力

### 成员管理 (`/member`)
- 用户列表 + 角色分配（`sys-user-role`）
- **数据范围由角色 `data_scope` 驱动**（禁止前端自选“全局/社团”随意组合）：
  | data_scope | 含义 | 分配时 scopeType / scopeId |
  |---|---|---|
  | 0 | 全部 | 均为 null |
  | 1 | 本学院 | scopeType=1 + 学院 id |
  | 2 | 本社团 | scopeType=2 + 社团 id（社团列表走 `/sys-club/list`，勿用门户） |
  | 3 | 本部门 | scopeType=3 + 部门 id（先选社团再选部门） |
  | 4 | 仅自己 | 均为 null |
- `role_code` 为常量（`SUPER_ADMIN` / `ADVISOR` / `ADVISOR_{类别}_{缩写}` 等），**分配时不得动态拼接/新建 role_code**；社团上下文只写在 `sys_user_role.scope_*`

### 通知公告 (`/notice`)
- 收件箱 / 发布（按等级）

### 数据统计 (`/statistics`)
- ECharts：活动量、成员趋势、分类占比等；成员数批量能力与 `/club-statistics/list` 对齐，勿依赖已废弃的 `listClubStatistics_F`

### 数据权限提示
- 预算可见性：`utils/permission.ts` 的 `canViewBudget` / `isBudgetReadonly`（基于 `effectiveLevel` + roleIds）
- 行级操作：优先使用后端下发的布尔权限字段（如 `canDissolve`），**禁止** `v-if="role === '社长'"` 硬编码

---

## 布局与动态菜单
- 左侧菜单：`GET` 菜单树 + `MainLayout` 中 `extraMenus` 按 `minLevel` 补齐
- `stores/menu.ts`：`STATIC_ROUTES` + `import.meta.glob` 解析 `componentPath`
- 顶部：用户下拉、通知铃、面包屑

---

## 权限等级体系（五级 RBAC）

### 等级定义（`src/utils/level.ts`）
| Level | 角色 | role_code | 说明 |
|---|---|---|---|
| 0 | 超级管理员 | `SUPER_ADMIN` | 全平台无限制 |
| 1 | 院长/指导老师 | `ADVISOR` / `ADVISOR_*` | 管辖范围数据 |
| 2 | 社团社长 | `CLUB_PRESIDENT` | 本社团数据 |
| 3 | 部门部长 | `CLUB_MINISTER` | 本部门数据 |
| 4 | 普通学生 | `MEMBER` | 仅本人数据 |
| 1 | 学院管理员 | `ADMIN` | 校级/学院管理 |

### role_code 命名规范（2026-07-24 统一）
- **全大写 + 下划线**，禁止数字后缀、大小写混用
- `SUPER_ADMIN` — 超级管理员
- `ADMIN` — 校级/学院管理员（可扩展为 `ADMIN_COLLEGE` 等）
- `ADVISOR` — 指导老师基础模板（`isAdvisorRoleCode` 匹配 `startsWith('ADVISOR')`）
- `ADVISOR_{类别缩写}_{社团英文缩写}` — 具体社团指导老师，类别缩写来自 `ClubCategory`（SZ/XS/CX/WH/GY/ZL），社团缩写取英文名首字母大写
- `CLUB_PRESIDENT` — 社长
- `CLUB_MINISTER` — 部长
- `MEMBER` — 普通成员

### 数据库 seed 角色（`mysql/mass_test1.sql`）
| id | role_name | role_code | role_level | data_scope |
|---|---|---|---|---|
| 1 | 超级管理员 | `SUPER_ADMIN` | 0 | 0 全部 |
| 2 | 普通成员 | `MEMBER` | 4 | 4 仅自己 |
| 3 | 指导老师 | `ADVISOR` | 1 | 2 本社团 |
| 4 | 学院管理员 | `ADMIN` | 1 | 1 本学院 |
| 5 | 社长 | `CLUB_PRESIDENT` | 2 | 2 本社团 |
| 6 | 部长 | `CLUB_MINISTER` | 3 | 3 本部门 |

### 后端两层拦截
1. **接口准入（AOP）**：`@RequiresLevel(minLevel)` → `effectiveLevel ≤ minLevel`
2. **数据范围过滤（Service）**：按等级注入 club / college / department / userId 条件

### 前端两层拦截
1. **路由守卫**：`effectiveLevel` vs `route.meta.minLevel`
2. **菜单可见性**：`userStore.effectiveLevel <= item.minLevel`

### 社团相关接口等级（摘要）
- `DEPT_LEADER(3)`：`/sys-club/list|detail|…`、`/sys-college/list`、`/club-statistics/list`
- `CLUB_LEADER(2)`：`POST /club-application/apply/dissolve`（业务内再校验指导老师/社长）
- `ADMIN(1)`：创建申请、合议签字、学院/校级审批
- `SUPER_ADMIN(0)`：发起合议

---

## 开发流程（按顺序）
1. 对齐后端真实 Controller 路径（以源码为准，不以旧 OpenAPI `_F` 为准）
2. 补齐 `src/api/*` 与 `types`
3. 更新 `stores/user` / `menu` 与路由 `minLevel`
4. 按模块实现页面；社团模块严格遵循 `社团权限过滤加强.md`
5. 联调：列表 Tab 联动、权限按钮显隐、成员数聚合、详情/合议闭环

## 注意事项
- 分页参数：`page` + `limit`；响应看 `data.records` / `data.total`
- `SysClub` 实体 `id` 可能 `@JsonIgnore`，管理端以 `SysClubListVO` 暴露 `id`（供成员数与合议跳转）
- 类别下拉必须走接口；色块用 `categoryCode`（SZ/XS/CX/WH/GY/ZL）
- 遇到 API 与文档不一致，以 Controller + 联调响应为准，并回写本 skill / 对应设计 md

---

## 2026-07-23 改动：Level 4 学生工作台权限修复与活动管理重构

### 问题背景
Level 4 学生登录后工作台持续弹出「权限不足」，原因是 `fetchDashboardBundle` 调用了当时要求过高的活动列表接口，且 KPI 跳转触发路由守卫。

### 改动清单

#### 后端
1. **`ActivityApplyController.java`** — `/list`、`/detail/{id}` 的 `@RequiresLevel` 降为 `Level.STUDENT`
2. **`ActivityApplyServiceImpl.java`** — Level 4 列表追加本人数据范围

#### 前端
3. **`stores/menu.ts`** — `activity-apply` 标题「活动管理」，`minLevel: 4`
4. **`views/dashboard/index.vue`** — KPI 路由准入与只读样式

### 当时 Level 4 行为快照
| 页面/功能 | 可访问 | 数据范围 |
|---|---|---|
| 工作台 | ✅ | KPI（活动总数、签到次数、未读通知） |
| 活动管理 `/activity/apply` | ✅ | 仅本人参与 |
| 签到管理 `/activity/sign` | ❌ (minLevel:2) | — |
| 社团管理 `/club` | ❌ | 菜单不可见（后已调整为 minLevel:3，仍对 Level 4 不可见） |
| 成员管理 `/member` | ❌ (minLevel:2) | — |
| 统计看板 `/statistics` | ❌ (minLevel:2) | — |
| 通知中心 `/notice` | ✅ | 收件箱 |
| 个人中心 `/profile` | ✅ | 本人 |

---

## 2026-07-23 改动：社团管理模块按 `社团权限过滤加强.md` 全量构建

### 设计对齐
依据 `.cursor/skills/社团权限过滤加强.md`（v1.1）：明确区分社长（Level 2）与部长（Level 3）权限边界，部长仅只读查看本部门挂靠社团的「正常社团」Tab，操作列仅「查看详情」；Tab 流程隔离、操作列后端权限标识、成员数前端批量聚合、详情/合议独立页、Level 4 无后台入口。

### 后端
1. **`SysClubController`** — `GET /sys-club/list|detail|departments|members|member-count`
2. **`SysCollegeController`** — `GET /sys-college/list`
3. **`ClubStatisticsController`** — `GET /club-statistics/list?clubIds=`
4. **`ClubApplicationController`** — `categories` 返回 `{code,label}`；解散申请允许指导老师或社长（`minLevel=CLUB_LEADER`）；并发解散冲突 7207
5. **`ClubCouncilController`** — 新增 `GET /list`、`GET /detail`
6. **VO / Service** — `SysClubListVO`（含 `advisorName`、`canDissolve`、`canSignCouncil`、`activeCouncilId` 等）、`SysClubServiceImpl.adminPageQuery(tabMode)`、`use` 批量成员数 SQL

### 前端
1. API：`sysClub` / `clubApplication` / `sysCollege` / `clubStatistics` / `clubCouncil`
2. `composables/useMemberCount.ts`
3. `views/club/list.vue`、`detail.vue`、`council.vue`
4. 路由与菜单：`club` **minLevel: 3**；详情/合议静态路由；`keep-alive` 包含 `ClubList`

### 验收要点
- [x] 类别下拉无前端硬编码枚举
- [x] 操作列由 `canDissolve` / `canSignCouncil` 驱动
- [x] 三 Tab 数据互不污染，解散成功自动切 Tab
- [x] 成员数统一走 `useMemberCount`
- [x] Level 4 无「社团管理」菜单；部长可见
