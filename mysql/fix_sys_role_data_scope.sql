-- 校准 sys_role.data_scope / role_level，与 UserRoleScopeHelper（0全部 1学院 2社团 3部门 4仅自己）一致
-- 在已有库上执行一次即可

UPDATE `sys_role` SET `role_level` = 0, `data_scope` = 0, `description` = '系统管理员' WHERE `role_code` = 'SUPER_ADMIN';
UPDATE `sys_role` SET `role_level` = 4, `data_scope` = 4, `description` = '普通成员' WHERE `role_code` = 'MEMBER';
UPDATE `sys_role` SET `role_level` = 1, `data_scope` = 2, `description` = '指导老师（基础模板，具体社团见 ADVISOR_类别_社团缩写）' WHERE `role_code` = 'ADVISOR';
UPDATE `sys_role` SET `role_level` = 1, `data_scope` = 1, `description` = '校级/学院管理员' WHERE `role_code` = 'ADMIN';
UPDATE `sys_role` SET `role_level` = 2, `data_scope` = 2, `description` = '社团社长' WHERE `role_code` = 'CLUB_PRESIDENT';
UPDATE `sys_role` SET `role_level` = 3, `data_scope` = 3, `description` = '部门部长' WHERE `role_code` = 'CLUB_MINISTER';
