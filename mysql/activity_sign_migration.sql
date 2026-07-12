-- 签到模块增量 DDL

CREATE TABLE IF NOT EXISTS `activity_sign_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `activity_id` bigint NOT NULL COMMENT '活动ID',
    `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用签到',
    `sign_mode` tinyint NOT NULL DEFAULT 1 COMMENT '1定位 2扫码 3两者',
    `sign_start_time` datetime NOT NULL COMMENT '签到开始',
    `sign_end_time` datetime NOT NULL COMMENT '签到结束',
    `sign_radius` int NOT NULL DEFAULT 100 COMMENT '签到半径米',
    `enable_checkout` tinyint NOT NULL DEFAULT 0 COMMENT '是否启用签退',
    `center_latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '签到中心纬度',
    `center_longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '签到中心经度',
    `qr_token` varchar(64) NULL DEFAULT NULL COMMENT '扫码签到令牌',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_activity_id` (`activity_id` ASC) USING BTREE
) ENGINE = InnoDB COMMENT = '活动签到配置';

CREATE TABLE IF NOT EXISTS `activity_sign_makeup` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `activity_id` bigint NOT NULL,
    `user_id` bigint NOT NULL COMMENT '补签人',
    `applicant_id` bigint NOT NULL COMMENT '发起人(社长)',
    `reason_type` tinyint NOT NULL COMMENT '1活动方 2个人 3特殊',
    `reason_detail` varchar(500) NULL DEFAULT NULL,
    `attachment` varchar(500) NULL DEFAULT NULL,
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '1待审 2通过 3驳回',
    `current_step` int NOT NULL DEFAULT 1,
    `approve_user_id` bigint NULL DEFAULT NULL COMMENT '当前审批人',
    `approve_opinion` varchar(500) NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_activity_user` (`activity_id`, `user_id`) USING BTREE
) ENGINE = InnoDB COMMENT = '补签申请';

ALTER TABLE `activity_sign`
    ADD COLUMN `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度' AFTER `sign_time`,
    ADD COLUMN `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度' AFTER `latitude`,
    ADD COLUMN `is_late` tinyint NOT NULL DEFAULT 0 COMMENT '是否迟到' AFTER `sign_status`,
    ADD COLUMN `is_early_leave` tinyint NOT NULL DEFAULT 0 COMMENT '是否早退' AFTER `is_late`,
    ADD COLUMN `checkout_time` datetime NULL DEFAULT NULL COMMENT '签退时间' AFTER `is_early_leave`,
    ADD COLUMN `operator_id` bigint NULL DEFAULT NULL COMMENT '手动签到操作人' AFTER `checkout_time`,
    ADD COLUMN `approver_id` bigint NULL DEFAULT NULL COMMENT '补签审批人' AFTER `operator_id`;
