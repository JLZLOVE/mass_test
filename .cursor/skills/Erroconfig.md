## 一、概述

`ErrorConfig` 集中管理业务异常码。命名：`{模块}_{描述}_CODE` / `_MSG`。

权威区间与清单以 `PROJECT_OVERVIEW.md` §9 为准；本文档为速查同步版（2026-07-18）。

------

## 二、编码区间分配

| 编码段 | 模块 | 说明 |
|---|---|---|
| `400` | 通用 | `BAD_REQUEST` |
| `1xxx` | 用户 / 注册 | 注册、用户名密码等 |
| `6001~6003` | 活动编号杂项 | 分类/内容/编号空 |
| **`64xx`** | **通知 / 模板** | 6401~6415（含 receiverValues、模板防篡改） |
| **`65xx`** | **活动签到** | 6501~6519 |
| `7001` | 通用校验 | 非法字符 |
| **`71xx`** | **菜单**（代码中多为 `91xx` 历史段，以 ErrorConfig 常量名为准） | 菜单 CRUD |
| **`72xx`** | **社团申请 / 合议** | 7201 申请不存在；**7202 社团不存在** |
| **`73xx`** | **活动审批** | 7301~7319（含门户防篡改封锁） |
| `8xxx` | 认证 | 登录态 / Token |
| `9xxx` | 角色权限 | 角色 CRUD、分配、禁用 |
| `91xx` | 菜单管理 | 与部分文档 71xx 并存时以代码为准 |

------

## 三、重点新增 / 常用码

### 通知 64xx（节选）

| 编码 | 常量 | 信息 |
|---|---|---|
| 6401 | `NOTICE_NOT_FOUND` | 通知不存在 |
| 6412 | `TEMPLATE_CODE_TAMPER` | 模板编码与创建时间不一致 |
| 6413 | `TEMPLATE_CODE_INVALID` | 模板编码格式无效 |
| 6414 | `NOTICE_RECEIVER_VALUES_INVALID` | receiverValues 须为 JSON 数组 |
| 6415 | `NOTICE_RECEIVER_EMPTY` | 接收范围不能为空 |

### 签到 65xx（节选）

| 编码 | 常量 | 信息 |
|---|---|---|
| 6501 | `SIGN_CONFIG_NOT_FOUND` | 签到未配置或未启用 |
| 6502 | `SIGN_ACTIVITY_NOT_FOUND` | 活动不存在或未通过审批 |
| 6511 | `SIGN_CONFLICT` | 同时段已签其他活动 |
| 6516 | `SIGN_START_BEFORE_NOW` | 签到开始不能早于当前 |
| 6517 | `SIGN_END_TOO_LATE` | 签到结束不能超过开始后 7 天 |
| 6518 | `SIGN_CHECKOUT_BEFORE_SIGN` | 签退不能早于签到 |
| 6519 | `SIGN_CHECKOUT_TOO_LATE` | 签退不能超过签到后 7 天 |

### 社团 72xx（节选）

| 编码 | 常量 | 信息 |
|---|---|---|
| 7201 | `CLUB_APPLY_NOT_FOUND` | 社团申请不存在 |
| 7202 | `CLUB_NOT_FOUND` | 社团不存在 |

### 活动审批 73xx（节选）

| 编码 | 常量 | 信息 |
|---|---|---|
| 7301 | `ACT_APPLY_NOT_FOUND` | 活动申请不存在 |
| 7319 | （门户防篡改） | activityNo 与 create_time 不一致封锁 |

### 认证 8xxx

| 编码 | 常量 | 信息 |
|---|---|---|
| 8001 | `LOGIN_INVALID` / `NOT_LOGGED_IN` | 未登录或会话已失效 |

### 角色 9xxx / 菜单 91xx

见 `PROJECT_OVERVIEW.md` §9.2 / §9.3；角色分配冲突 `9011` 等。

------

## 四、维护约定

- 新增业务码：先改 `ErrorConfig.java`，再同步 `PROJECT_OVERVIEW.md` §9 与本文档。
- 禁止复用已废弃区间（如旧文档中的签到 94xx/95xx）。
