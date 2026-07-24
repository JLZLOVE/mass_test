-- 立即修复：把 data_scope 写成与代码一致的值（0全部 1学院 2社团 3部门 4仅自己）
-- 在 MySQL 客户端执行后，重启 Spring Boot

UPDATE sys_role SET data_scope = 0, role_level = 0 WHERE role_code = 'SUPER_ADMIN';
UPDATE sys_role SET data_scope = 4, role_level = 4 WHERE role_code = 'MEMBER';
UPDATE sys_role SET data_scope = 2, role_level = 1 WHERE role_code = 'ADVISOR';
UPDATE sys_role SET data_scope = 1, role_level = 1 WHERE role_code = 'ADMIN';
UPDATE sys_role SET data_scope = 2, role_level = 2 WHERE role_code = 'CLUB_PRESIDENT';
UPDATE sys_role SET data_scope = 3, role_level = 3 WHERE role_code = 'CLUB_MINISTER';

-- 核对
SELECT id, role_name, role_code, role_level, data_scope FROM sys_role ORDER BY id;
