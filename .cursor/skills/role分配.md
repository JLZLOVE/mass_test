# 后端权限系统设计任务（角色 + 数据范围）

## 目标
实现基于 **角色（Role）** 与 **数据范围（Scope）** 的权限控制，所有权限分配由管理员在后端接口完成，不涉及前端页面。

## 核心表结构
- PROJECT_OVERVIEW.md看2. 数据库设计,有需要的数据基本在这个文件中

## 关键业务规则（Service 层校验）
1. 分配角色时，需根据 `role_code` 与 `user_type` 进行匹配：
   - `role_code = 'PRESIDENT'`（社长） → 仅允许 `user_type = 'STUDENT'`
   - `role_code = 'ADMIN'`（管理员） → 仅允许 `user_type = 'TEACHER'`
   - 其他角色可自定义规则，不匹配则抛出异常。
2. 分配时必须指定 `scope_type` 和 `scope_id`（不能为空），确保数据隔离。

## 后端接口需求
- 提供 `post` 接口，入参：`userId`, `roleId`, `scopeType`, `scopeId`
- 执行校验后，插入 `sys_user_role` 记录
- 同时提供 `DELETE` 接口撤销授权

## 权限自动生效机制
- 用户登录时，从 `sys_user_role` 加载其所有角色及对应的 `scope_id`，存入 `LoginUserDetails`
- 使用 AOP 切面 `@RequiresLevel(minLevel)` 校验角色等级
- 使用 `DataScopeHelper` 自动将当前用户的 `scope_id` 拼接到数据查询 SQL 中（通过 MyBatis 拦截器或手动注入）
- 所有数据查询接口需根据当前用户的 `scope_id` 过滤结果，实现数据隔离。

## 备注
- 角色定义（`sys_role`）由管理员预先维护，不涉及代码变更。
- 所有权限逻辑完成后，仅需通过接口分配记录即可驱动整个权限体系。