-- 活动审批模块增量 DDL（用户侧数据库已执行时可忽略）

ALTER TABLE `activity_apply`
    ADD COLUMN `activity_level` tinyint NULL DEFAULT NULL COMMENT '1院级 2校级' AFTER `approve_status`,
    ADD COLUMN `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁' AFTER `activity_level`,
    ADD COLUMN `attachment` varchar(500) NULL DEFAULT NULL COMMENT '申请附件路径' AFTER `safety_plan`,
    ADD COLUMN `level_adjust_locked` tinyint NOT NULL DEFAULT 0 COMMENT '级别是否已调整锁定' AFTER `activity_level`,
    ADD COLUMN `summary_content` text NULL COMMENT '活动总结文字' AFTER `reject_reason`,
    ADD COLUMN `summary_attachment` varchar(500) NULL DEFAULT NULL COMMENT '总结附件路径' AFTER `summary_content`,
    ADD COLUMN `summary_upload_time` datetime NULL DEFAULT NULL COMMENT '总结上传时间' AFTER `summary_attachment`;

ALTER TABLE `activity_approve_flow`
    ADD COLUMN `flow_type` tinyint NOT NULL DEFAULT 1 COMMENT '1正常审批 2变更审批' AFTER `activity_id`,
    ADD COLUMN `step_enter_time` datetime NULL DEFAULT NULL COMMENT '进入该步骤时间' AFTER `approve_time`;

CREATE TABLE IF NOT EXISTS `activity_apply_history` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `activity_apply_id` bigint NOT NULL COMMENT '原活动申请ID',
    `activity_no` varchar(50) NOT NULL,
    `club_id` bigint NOT NULL,
    `activity_name` varchar(200) NOT NULL,
    `category_id` bigint NULL DEFAULT NULL,
    `activity_type` tinyint NULL DEFAULT NULL,
    `start_time` datetime NOT NULL,
    `end_time` datetime NOT NULL,
    `location` varchar(200) NOT NULL,
    `location_detail` varchar(500) NULL DEFAULT NULL,
    `expected_people` int NULL DEFAULT NULL,
    `budget` decimal(10, 2) NULL DEFAULT NULL,
    `activity_content` text NULL,
    `safety_plan` text NULL,
    `attachment` varchar(500) NULL DEFAULT NULL,
    `activity_level` tinyint NULL DEFAULT NULL,
    `history_reason` varchar(500) NULL DEFAULT NULL COMMENT '变更原因',
    `new_start_time` datetime NULL DEFAULT NULL COMMENT '变更后开始时间',
    `new_end_time` datetime NULL DEFAULT NULL COMMENT '变更后结束时间',
    `new_location` varchar(200) NULL DEFAULT NULL COMMENT '变更后地点',
    `new_location_detail` varchar(500) NULL DEFAULT NULL COMMENT '变更后详细位置',
    `history_status` tinyint NOT NULL DEFAULT 1 COMMENT '1变更审批中 2已通过 3已驳回',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_activity_apply_id` (`activity_apply_id` ASC) USING BTREE
) ENGINE = InnoDB COMMENT = '活动变更历史快照';
