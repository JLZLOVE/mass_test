-- 前台门户展示方案 - 数据库迁移脚本
-- 执行日期：2026-07-16

-- ==================== notice_info 表 ====================

-- 1. 通知编号（对外标识，不暴露自增主键ID）
ALTER TABLE notice_info ADD COLUMN notice_no VARCHAR(32) DEFAULT NULL COMMENT '通知编号（NT{yyyyMMddHHmm}_{6位随机}）';
CREATE UNIQUE INDEX idx_notice_no ON notice_info(notice_no);

-- 2. 通知题图路径
ALTER TABLE notice_info ADD COLUMN cover_image VARCHAR(500) DEFAULT NULL COMMENT '通知题图路径';

-- 3. 浏览量
ALTER TABLE notice_info ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览量（含匿名访问）';

-- 4. 接收总人数
ALTER TABLE notice_info ADD COLUMN receiver_count INT DEFAULT 0 COMMENT '接收总人数（发布时预计算）';

-- ==================== activity_apply 表 ====================

-- 5. 活动封面图路径
ALTER TABLE activity_apply ADD COLUMN cover_image VARCHAR(500) DEFAULT NULL COMMENT '活动封面图路径';

-- 6. 主办/承办/指派说明
ALTER TABLE activity_apply ADD COLUMN organizer_note VARCHAR(500) DEFAULT NULL COMMENT '主办/承办/指派说明';

-- ==================== sys_club 表 ====================

-- 7. 社团解散时间
ALTER TABLE sys_club ADD COLUMN dissolve_time DATETIME DEFAULT NULL COMMENT '社团解散时间';