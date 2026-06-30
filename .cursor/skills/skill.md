```markdown
---
name: club-admin-frontend
description: 社团综合管理平台前端开发技能。基于 Vue3 + TypeScript + Element Plus + Vite，实现社团/活动/审批/通知/统计/用户权限模块。API 严格遵循提供的 OpenAPI 规范，统计图表按月度/学期展示。
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
│   ├── api/               # 自动/手动生成的接口函数
│   ├── assets/
│   ├── components/        # 通用组件（如权限按钮、图表容器）
│   ├── layouts/           # 布局（左侧菜单 + 右侧内容 + 顶部栏）
│   ├── router/            # 路由配置
│   ├── stores/            # Pinia store（用户、菜单、权限）
│   ├── types/             # TypeScript 类型定义（从 OpenAPI 生成或手动）
│   ├── utils/             # 请求拦截器、权限指令、格式化
│   ├── views/
│   │   ├── login/
│   │   ├── club/          # 社团管理
│   │   ├── activity/      # 活动申请 / 审批 / 签到
│   │   ├── member/        # 成员管理 + 角色分配
│   │   ├── notice/        # 通知公告
│   │   └── statistics/    # 数据统计（图表）
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## API 集成规则（基于 OpenAPI）

### 1. 请求客户端封装 (`src/utils/request.ts`)
- 基础 URL: `http://localhost:100/Mass_Test` （可从 OpenAPI servers 读取）
- 请求头自动添加 `Authorization: Bearer ${token}`（token 存储在 localStorage）
- 响应拦截器统一判断 `code === 200` 为成功，否则弹出错误提示
- 登录接口特殊处理：**使用 `GET` 方法，参数为 query `name` 和 `password`**（不是 JSON body）

### 2. 接口函数生成策略
由于 OpenAPI 文档中存在大量 `updateSysUser_F` / `add_B` 等命名不规范接口，请按以下规则**手动创建**或**使用脚本简化**：
- **社团管理**：使用 `/sys-club/*` 路径下的 `listSysClub_F`（分页列表）、`add_F`（新增）、`updateSysClub_F`（编辑）、`deleteSysClub_F/{id}`（解散）
- **活动申请**：使用 `/activity-apply/*`：`listActivityApply_F`（分页）、`add_F`（申请）、`updateActivityApply_F`（编辑）、`deleteActivityApply_F/{id}`（撤销）
- **审批流程**：`/activity-approve-flow/*`：`listActivityApproveFlow_F`（某活动的审批步骤）、`updateActivityApproveFlow_F`（同意/驳回，需传 `approveResult` 和 `approveOpinion`）
- **签到记录**：`/activity-sign/*`：`listActivitySign_F`（分页），手动补签调用 `add_F` 新增签到记录（`signType=3`）
- **用户/角色**：`/sys-user/*`、`/sys-user-role/*` 对应成员管理和角色分配
- **通知公告**：`/notice-info/*`
- **社团统计**：`/club-statistics/*`（获取统计数据用于图表）
- **菜单**：`/sys-menu/listSysMenu` 返回菜单树（`parentId` 为 0 表示根节点）

> 提示：所有分页接口的查询参数格式不明确，统一设计为：`pageNum`, `pageSize` + 实体对象字段（如 `sysClub`）。请参考 OpenAPI 中的 `parameters` 定义。

### 3. TypeScript 类型
- 根据 OpenAPI `components/schemas` 手动或使用 `openapi-typescript` 生成 `src/types/generated.ts`
- 响应格式：`R<T>`，包含 `code: number`, `data: T`, `message: string`

## 业务模块详细需求（必须实现）

### 登录认证
- 页面：`/login`，表单字段：`name`（用户名）、`password`（密码）
- 调用 `GET /login/allocation?name=xxx&password=xxx`，成功后存储 `token` 和用户基本信息（角色、社团ID等）
- 跳转到首页（动态菜单加载）

### 社团管理 (`/club`)
- 列表页：展示 `SysClub` 字段（id, clubName, category, collegeId, advisorId, status等）
- 支持按社团名称、状态筛选，分页
- 操作按钮：新增、编辑、解散（仅社长/管理员可见）
- 新增/编辑弹窗：包含字段 `clubName`, `clubCode`, `category`, `collegeId`, `advisorId`, `description`, `logo`（可选）

### 活动管理
#### 活动申请 (`/activity/apply`)
- 列表：显示所有申请（`ActivityApply`），支持搜索（活动名称、状态）
- 状态：草稿(1)、待审批(2)、审批中(3)、已通过(4)、已驳回(5)、已取消(6)
- 操作：新增、编辑（仅草稿状态）、撤销（删除）、查看审批流程图
- 新增/编辑表单字段：依据 `ActivityApply` 定义（activityName, categoryId, activityType, startTime, endTime, location, locationDetail, expectedPeople, budget, activityContent, safetyPlan）
- 注意：`activityNo` 后端自动生成，前端不填写

#### 审批流程 (`/activity/approve-flow/:applyId`)
- 展示某个活动申请的审批步骤列表（`ActivityApproveFlow` 按 `step` 排序）
- 使用 `el-steps` 组件，显示每一步的审批角色、审批人、结果、意见、时间
- 如果当前登录用户是当前步骤的审批人，显示“同意/驳回”按钮，调用 `updateActivityApproveFlow_F` 接口（需传 `id`, `approveResult`, `approveOpinion`）

#### 签到记录 (`/activity/sign/:activityId`)
- 列表显示已签到用户（`ActivitySign` 字段：userName, realName, signTime, signType, signStatus）
- 手动补签：弹出用户选择器（从活动报名名单或社团成员中选取），调用 `add_F` 接口新增签到记录（`signType=3`）

### 成员管理 (`/member`)
- 用户列表（`SysUser`）：展示 username, realName, gender, phone, email, userType, status
- 支持按社团、角色筛选（需要组合 `sys_user_role` 查询）
- 角色分配：点击用户，弹出对话框显示其当前角色列表（`SysUserRole`），可新增/删除角色（调用 `/sys-user-role/add_F` 和 `deleteSysUserRole_F`）
- 角色预设：社长(role_id=2)、部长(3)、普通成员(4)、指导老师(5)。注意 `scope_type` 和 `scope_id` 用于确定所属社团/部门。

### 通知公告 (`/notice`)
- 列表：`NoticeInfo` 字段（title, categoryId, importance, urgency, publishTime, status）
- 操作：发布（新增）、编辑、撤回（删除）、查看已读确认列表
- 发布表单：标题、内容（富文本）、分类、重要程度、紧急程度、接收类型（全体/指定角色/指定社团/指定人员）、是否需要确认阅读、过期时间
- 已读确认列表：展示 `NoticeReadRecord`，包含用户姓名、阅读时间、确认时间、是否确认

### 数据统计 (`/statistics`) **（严格按以下要求）**
- **活动数量按月柱状图**：
  - 调用 `/club-statistics/listClubStatistics_F` 获取所有社团的月度统计数据（`stat_date` 字段为 date）。
  - 前端聚合：按月份（YYYY-MM）分组，统计每月活动数量（`activityCount` 总和）。
  - 使用 ECharts 柱状图展示，X 轴为月份，Y 轴为活动次数。
- **成员人数趋势折线图（逐月）**：
  - 从相同数据源提取 `total_members`（总成员数）按月份聚合。
  - 折线图展示近 12 个月成员人数变化。
- **社团分类占比饼图（每学期更新）**：
  - 获取所有社团（`/sys-club/listSysClub`），根据 `category` 字段（学术科技/文化体育/公益等）统计数量。
  - 注意：饼图数据不需要实时更新，可在每学期初手动刷新或前端缓存。
- **其他辅助统计**：可展示社团总数、活动总数、参与人次总和等（从 `club_statistics` 聚合）。
- **页面布局**：上方筛选区域（社团下拉、日期范围），下方三个主要图表区域。

### 数据权限提示（前端实现）
- 后端提供了 `sys_data_permission` 表，但前端可先简化处理：
  - 普通成员（`role_id=4`）隐藏“预算金额”字段（如社团列表的 `budget`、活动申请的 `budget`）。
  - 部长（`role_id=3`）可编辑部分数据，但预算只读。
  - 社长（`role_id=2`）及管理员可见全部。
- 实现方式：在组件中使用自定义指令 `v-permission` 或计算属性 `userRole` 控制 `v-if`。
- 更精细的行级权限可后续对接后端接口动态返回字段可见性。

## 布局与动态菜单
- 左侧菜单递归渲染，数据源来自 `GET /sys-menu/listSysMenu`（注意 `menuType`：1目录/2菜单/3按钮）
- 构建菜单树：`parentId === 0` 为顶级，根据 `componentPath` 动态加载路由（使用 `import.meta.glob`）
- 顶部栏：显示用户头像/名称，退出登录，暗色/亮色切换（可选）

## 开发流程（按顺序执行）
1. 创建项目：`npm create vue@latest`，选择 TypeScript、Vue Router、Pinia。
2. 安装依赖：`axios`, `element-plus`, `echarts`, `@vueuse/core`。
3. 配置 `vite.config.ts` 代理解决跨域（可选）。
4. 实现 `src/utils/request.ts` 和 `src/api/...`（根据 OpenAPI 手动编写接口函数，约 10 个主要模块）。
5. 生成 `src/types/generated.ts`（拷贝 OpenAPI 中的 schemas 并手动调整）。
6. 实现 `src/stores/user.ts` 和 `src/stores/menu.ts`，完成登录和菜单动态加载。
7. 按顺序开发页面：登录 → 社团管理 → 活动申请 → 审批流程 → 签到记录 → 成员管理 → 通知公告 → 数据统计。
8. 在每个页面中使用 Element Plus 组件，确保响应式布局。

## 注意事项
- 所有接口的 `update` 操作均对应 PUT 请求，`add` 对应 POST，`delete` 对应 DELETE。
- 分页接口的返回值结构未在 OpenAPI 中明确定义，请假设后端返回 `{ records: T[], total: number }` 包装在 `data` 中。
- 若遇到字段缺失（如用户名、社团名关联），可通过额外接口联查，或在前端 store 中缓存字典（如社团列表、角色列表）。
- 遇到任何不符合预期的 API 行为，请在第二轮对话中提供具体的接口请求/响应示例，我会指导调整。

--- 

```