-- 通知模块增量 DDL

ALTER TABLE `notice_info`
    ADD COLUMN `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶' AFTER `status`,
    ADD COLUMN `pin_expire_at` datetime NULL DEFAULT NULL COMMENT '置顶结束时间' AFTER `is_pinned`,
    ADD COLUMN `long_term_visible` tinyint NOT NULL DEFAULT 1 COMMENT '1长期可见 0仅当前成员' AFTER `pin_expire_at`,
    ADD COLUMN `attachments` json NULL COMMENT '附件路径列表' AFTER `long_term_visible`,
    ADD COLUMN `attachment_min_level` tinyint NULL DEFAULT 4 COMMENT '附件最低等级0-4' AFTER `attachments`,
    ADD COLUMN `revocable` tinyint NOT NULL DEFAULT 1 COMMENT '0不可撤回' AFTER `attachment_min_level`,
    ADD COLUMN `source_type` tinyint NOT NULL DEFAULT 0 COMMENT '0手动 1活动取消自动' AFTER `revocable`,
    ADD COLUMN `template_id` bigint NULL DEFAULT NULL COMMENT '使用的模板ID' AFTER `source_type`,
    ADD COLUMN `scheduled_publish_time` datetime NULL DEFAULT NULL COMMENT '定时发送时间' AFTER `publish_time`;

CREATE TABLE IF NOT EXISTS `notice_template` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `template_name` varchar(100) NOT NULL COMMENT '模板编码：前缀_yyyyMMddHHmm_6位随机数',
    `title` varchar(200) NOT NULL COMMENT '标题模板',
    `content` longtext NOT NULL COMMENT '内容模板',
    `category_id` bigint NULL DEFAULT NULL COMMENT '默认分类',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_template_name`(`template_name` ASC) USING BTREE
) ENGINE = InnoDB COMMENT = '通知模板';
