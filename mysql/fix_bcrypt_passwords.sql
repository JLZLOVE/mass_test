-- 可选后置脚本：将测试账号密码更新为 BCrypt 哈希
-- 使用前请先用 BCryptPasswordEncoder.encode("123456") 生成哈希，替换下方占位符
-- 执行：mysql -uroot -p mass_test1 < mysql/fix_bcrypt_passwords.sql

USE mass_test1;

-- UPDATE sys_user SET password = '<BCrypt哈希>' WHERE username IN ('admin', '2022002');

-- 验证标准用学号（注册接口会自动加密，也可手动插入）
-- INSERT INTO sys_user (username, password, real_name, gender, user_type, status)
-- VALUES ('202320164602', '<BCrypt哈希>', '测试学生', 1, 1, 1);
