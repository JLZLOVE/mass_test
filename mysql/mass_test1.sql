/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : mass_test1

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 04/07/2026 17:33:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_apply
-- ----------------------------
DROP TABLE IF EXISTS `activity_apply`;
CREATE TABLE `activity_apply`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动编号',
  `club_id` bigint NOT NULL COMMENT '主办社团',
  `activity_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '活动分类',
  `activity_type` tinyint NULL DEFAULT NULL COMMENT '1:日常活动 2:比赛 3:演出 4:讲座 5:其他',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动地点',
  `location_detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细位置（用于定位）',
  `expected_people` int NULL DEFAULT NULL COMMENT '预计参与人数',
  `budget` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算金额',
  `activity_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动内容',
  `safety_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '安全预案',
  `apply_user_id` bigint NOT NULL COMMENT '申请人',
  `apply_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `current_approve_step` int NULL DEFAULT 0 COMMENT '当前审批步骤',
  `approve_status` tinyint NULL DEFAULT 1 COMMENT '1:草稿 2:待审批 3:审批中 4:已通过 5:已驳回 6:已取消',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `activity_no`(`activity_no` ASC) USING BTREE,
  INDEX `idx_club_status`(`club_id` ASC, `approve_status` ASC) USING BTREE,
  INDEX `idx_apply_time`(`apply_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_apply
-- ----------------------------
INSERT INTO `activity_apply` VALUES (1, 'ACT2025001', 1, '人工智能讲座', 1, 1, '2025-04-10 14:00:00', '2025-04-10 17:00:00', '教学楼101', NULL, 50, 200.00, 'AI前沿技术分享', '应急预案', 1, '2026-02-11 21:48:57', 1, 4, NULL, '2026-02-11 21:48:57', NULL);
INSERT INTO `activity_apply` VALUES (2, 'ACT2025002', 2, '社区清洁志愿活动', 3, 5, '2025-04-11 09:00:00', '2025-04-11 12:00:00', '阳光社区', '中心广场', 30, 0.00, '清理垃圾、宣传环保', '安全须知', 3, '2026-02-11 21:48:57', 0, 1, NULL, '2026-02-11 21:48:57', NULL);
INSERT INTO `activity_apply` VALUES (3, 'ACT2025003', 1, '编程马拉松', 2, 2, '2025-04-15 08:00:00', '2025-04-15 20:00:00', '实训楼301', NULL, 20, 500.00, '24小时编程挑战', '医疗点设置', 2, '2026-02-11 21:48:57', 2, 2, NULL, '2026-02-11 21:48:57', NULL);

-- ----------------------------
-- Table structure for activity_approve_flow
-- ----------------------------
DROP TABLE IF EXISTS `activity_approve_flow`;
CREATE TABLE `activity_approve_flow`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `step` int NOT NULL COMMENT '步骤序号',
  `approve_role_id` bigint NULL DEFAULT NULL COMMENT '审批角色ID',
  `approve_user_id` bigint NULL DEFAULT NULL COMMENT '实际审批人ID',
  `approve_result` tinyint NULL DEFAULT NULL COMMENT '1:通过 2:驳回',
  `approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity`(`activity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动审批流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_approve_flow
-- ----------------------------
INSERT INTO `activity_approve_flow` VALUES (1, 1, 1, 5, 3, 1, '同意', '2026-02-11 21:48:57', '2026-02-11 21:48:57');
INSERT INTO `activity_approve_flow` VALUES (2, 1, 2, 2, 1, 1, '通过', '2026-02-11 21:48:57', '2026-02-11 21:48:57');
INSERT INTO `activity_approve_flow` VALUES (3, 3, 1, 5, 3, NULL, NULL, NULL, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for activity_category
-- ----------------------------
DROP TABLE IF EXISTS `activity_category`;
CREATE TABLE `activity_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `approve_flow_id` bigint NULL DEFAULT NULL COMMENT '审批流程模板ID',
  `need_location` tinyint NULL DEFAULT 1 COMMENT '是否需要定位签到',
  `need_report` tinyint NULL DEFAULT 0 COMMENT '是否需要活动报告',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_category
-- ----------------------------
INSERT INTO `activity_category` VALUES (1, '技术讲座', NULL, 1, 0, 1, '2026-02-11 21:48:57');
INSERT INTO `activity_category` VALUES (2, '文体比赛', NULL, 0, 1, 1, '2026-02-11 21:48:57');
INSERT INTO `activity_category` VALUES (3, '志愿服务', NULL, 1, 1, 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for activity_sign
-- ----------------------------
DROP TABLE IF EXISTS `activity_sign`;
CREATE TABLE `activity_sign`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `sign_type` tinyint NULL DEFAULT NULL COMMENT '1:自动定位 2:手动签到 3:补签',
  `sign_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `sign_location` point NULL COMMENT '签到经纬度',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到地址文本',
  `sign_status` tinyint NULL DEFAULT 1 COMMENT '1:正常 2:迟到 3:早退',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动签到表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_sign
-- ----------------------------
INSERT INTO `activity_sign` VALUES (1, 1, 2, 1, '2026-02-11 21:48:57', ST_GeomFromText('POINT(116.397 39.907)'), '教学楼101', 1, '2026-02-11 21:48:57');
INSERT INTO `activity_sign` VALUES (2, 1, 3, 1, '2026-02-11 21:48:57', ST_GeomFromText('POINT(116.397 39.907)'), '教学楼101', 1, '2026-02-11 21:48:57');
INSERT INTO `activity_sign` VALUES (3, 2, 1, 2, '2026-02-11 21:48:57', NULL, '阳光社区', 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for club_application
-- ----------------------------
DROP TABLE IF EXISTS `club_application`;
CREATE TABLE `club_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请编号',
  `apply_type` tinyint NOT NULL COMMENT '1:创建 2:解散',
  `club_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团名称（创建时必填）',
  `college_id` bigint NULL DEFAULT NULL COMMENT '挂靠学院',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团类别',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '社团简介',
  `proposed_leader_id` bigint NULL DEFAULT NULL COMMENT '拟定社长ID',
  `max_members` int NULL DEFAULT NULL COMMENT '最大招募人数',
  `dissolve_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '解散原因',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID（指导老师）',
  `applicant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '1:待学院审批 2:学院已通过 3:已通过 4:已驳回 5:已撤回',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `college_approver_id` bigint NULL DEFAULT NULL,
  `college_approve_time` datetime NULL DEFAULT NULL,
  `college_approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `admin_approver_id` bigint NULL DEFAULT NULL,
  `admin_approve_time` datetime NULL DEFAULT NULL,
  `admin_approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_apply_no`(`application_no` ASC) USING BTREE,
  INDEX `idx_applicant`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_college`(`college_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_application
-- ----------------------------

-- ----------------------------
-- Table structure for club_council
-- ----------------------------
DROP TABLE IF EXISTS `club_council`;
CREATE TABLE `club_council`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `club_id` bigint NOT NULL COMMENT '待解散社团ID',
  `initiator_id` bigint NOT NULL COMMENT '发起人ID（超管）',
  `initiator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `college_id` bigint NOT NULL COMMENT '社团挂靠学院',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解散原因',
  `status` tinyint NULL DEFAULT 1 COMMENT '1:合议中 2:已通过 3:已驳回',
  `signatories` json NULL COMMENT '签名人ID列表（含角色信息）',
  `executed_at` datetime NULL DEFAULT NULL COMMENT '执行时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_club`(`club_id` ASC) USING BTREE,
  INDEX `idx_college`(`college_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_council
-- ----------------------------

-- ----------------------------
-- Table structure for club_statistics
-- ----------------------------
DROP TABLE IF EXISTS `club_statistics`;
CREATE TABLE `club_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `club_id` bigint NOT NULL,
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_members` int NULL DEFAULT 0 COMMENT '当前总成员数',
  `new_members` int NULL DEFAULT 0 COMMENT '新增成员数',
  `activity_count` int NULL DEFAULT 0 COMMENT '活动次数',
  `total_participants` int NULL DEFAULT 0 COMMENT '参与人次',
  `total_budget` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总预算',
  `avg_score` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '活动平均评分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_club_date`(`club_id` ASC, `stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社团统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of club_statistics
-- ----------------------------
INSERT INTO `club_statistics` VALUES (1, 1, '2026-02-11', 15, 2, 3, 45, 1200.00, 4.50, '2026-02-11 21:48:57');
INSERT INTO `club_statistics` VALUES (2, 1, '2026-02-10', 13, 1, 1, 20, 200.00, 4.20, '2026-02-11 21:48:57');
INSERT INTO `club_statistics` VALUES (3, 2, '2026-02-11', 25, 5, 2, 60, 300.00, 4.80, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for notice_category
-- ----------------------------
DROP TABLE IF EXISTS `notice_category`;
CREATE TABLE `notice_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `priority` tinyint NULL DEFAULT 0 COMMENT '默认优先级',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice_category
-- ----------------------------
INSERT INTO `notice_category` VALUES (1, '社团通知', 1, 'notification', 1, '2026-02-11 21:48:57');
INSERT INTO `notice_category` VALUES (2, '活动通知', 2, 'activity', 1, '2026-02-11 21:48:57');
INSERT INTO `notice_category` VALUES (3, '系统公告', 0, 'system', 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for notice_info
-- ----------------------------
DROP TABLE IF EXISTS `notice_info`;
CREATE TABLE `notice_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容（富文本）',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID',
  `publisher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人姓名（冗余）',
  `importance` tinyint NULL DEFAULT 1 COMMENT '重要程度 1:低 2:中 3:高',
  `urgency` tinyint NULL DEFAULT 1 COMMENT '紧急程度 1:不紧急 2:紧急',
  `receiver_type` tinyint NOT NULL COMMENT '接收类型 1:全体学生 2:全体老师 3:指定角色 4:指定社团 5:指定人员',
  `receiver_values` json NULL COMMENT '接收者值（角色ID/社团ID/用户ID列表）',
  `need_confirm` tinyint NULL DEFAULT 0 COMMENT '是否需要确认阅读',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '0:草稿 1:已发布 2:已撤回',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice_info
-- ----------------------------
INSERT INTO `notice_info` VALUES (1, '计算机协会招新通知', '<p>本学期招新开始...</p>', 1, 1, '张三', 2, 1, 4, '[1]', 1, '2026-02-11 21:48:57', '2026-02-18 21:48:57', 1, '2026-02-11 21:48:57', NULL);
INSERT INTO `notice_info` VALUES (2, '青年志愿者协会活动', '<p>社区服务活动...</p>', 2, 3, '王老师', 1, 2, 2, '[]', 0, '2026-02-11 21:48:57', NULL, 1, '2026-02-11 21:48:57', NULL);
INSERT INTO `notice_info` VALUES (3, '系统维护通知', '<p>本周六凌晨维护...</p>', 3, 5, '管理员', 3, 1, 1, '[]', 0, '2026-02-11 21:48:57', '2026-02-12 21:48:57', 1, '2026-02-11 21:48:57', NULL);

-- ----------------------------
-- Table structure for notice_read_record
-- ----------------------------
DROP TABLE IF EXISTS `notice_read_record`;
CREATE TABLE `notice_read_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notice_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `read_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间（如果need_confirm=1）',
  `is_confirmed` tinyint NULL DEFAULT 0 COMMENT '是否已确认',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知阅读记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice_read_record
-- ----------------------------
INSERT INTO `notice_read_record` VALUES (1, 1, 2, '2026-02-11 21:48:57', NULL, 0);
INSERT INTO `notice_read_record` VALUES (2, 1, 3, '2026-02-11 21:48:57', '2026-02-11 21:48:57', 1);
INSERT INTO `notice_read_record` VALUES (3, 2, 1, '2026-02-11 21:48:57', NULL, 0);

-- ----------------------------
-- Table structure for sys_club
-- ----------------------------
DROP TABLE IF EXISTS `sys_club`;
CREATE TABLE `sys_club`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `club_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '社团名称',
  `club_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团编号',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团类别（学术科技/文化体育/公益等）',
  `college_id` bigint NULL DEFAULT NULL COMMENT '挂靠学院（可为空）',
  `advisor_id` bigint NULL DEFAULT NULL COMMENT '指导老师ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '社团简介',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团logo',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 0:解散 1:正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `club_code`(`club_code` ASC) USING BTREE,
  INDEX `idx_college`(`college_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社团表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_club
-- ----------------------------
INSERT INTO `sys_club` VALUES (1, '计算机协会', 'CLUB001', '学术科技', 1, 3, '计算机技术交流社团', NULL, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_club` VALUES (2, '青年志愿者协会', 'CLUB002', '公益', NULL, NULL, '志愿服务', NULL, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_club` VALUES (3, '舞蹈社', 'CLUB003', '文化体育', 3, NULL, '街舞、民族舞', NULL, 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_college
-- ----------------------------
DROP TABLE IF EXISTS `sys_college`;
CREATE TABLE `sys_college`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `college_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学院名称',
  `college_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院代码',
  `dean_id` bigint NULL DEFAULT NULL COMMENT '院长ID（关联sys_user）',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `college_code`(`college_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学院表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_college
-- ----------------------------
INSERT INTO `sys_college` VALUES (1, '计算机学院', 'CS', 3, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_college` VALUES (2, '经济管理学院', 'EM', 4, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_college` VALUES (3, '人文学院', 'HS', NULL, 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_data_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_permission`;
CREATE TABLE `sys_data_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `table_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '表名',
  `field_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段名',
  `visible` tinyint NULL DEFAULT 1 COMMENT '是否可见 0:隐藏 1:可见 2:只读',
  `condition_type` tinyint NULL DEFAULT NULL COMMENT '行级条件 1:全部 2:本部门 3:本人',
  `condition_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义条件',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据权限规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_data_permission
-- ----------------------------
INSERT INTO `sys_data_permission` VALUES (1, 3, 'sys_user', 'id_card', 0, NULL, NULL, '部长不可见身份证号', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_data_permission` VALUES (2, 2, 'activity_apply', 'budget', 2, 3, NULL, '社长可见预算但只读', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_data_permission` VALUES (3, 4, 'club_statistics', 'total_budget', 0, 3, NULL, '普通成员不可见预算', 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `club_id` bigint NOT NULL COMMENT '所属社团',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门ID（用于部门层级）',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '部长ID',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门职责',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_club`(`club_id` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社团部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (1, '办公室', 1, 0, 1, '负责社团日常事务', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_department` VALUES (2, '技术部', 1, 0, 2, '技术培训与项目开发', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_department` VALUES (3, '档案组', 1, 1, NULL, '文档管理', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_department` VALUES (4, '外联部', 2, 0, 3, '对外联络', 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_major
-- ----------------------------
DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '专业名称',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业代码',
  `college_id` bigint NOT NULL COMMENT '所属学院',
  `head_teacher_id` bigint NULL DEFAULT NULL COMMENT '专业负责人ID',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `major_code`(`major_code` ASC) USING BTREE,
  INDEX `idx_college`(`college_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专业表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_major
-- ----------------------------
INSERT INTO `sys_major` VALUES (1, '计算机科学与技术', 'CS001', 1, 3, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_major` VALUES (2, '软件工程', 'CS002', 1, NULL, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_major` VALUES (3, '国际经济与贸易', 'EM001', 2, 4, 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '类型 1:目录 2:菜单 3:按钮',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识（如 user:list, activity:approve）',
  `component_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径',
  `route_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_permission`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', 1, NULL, NULL, '/system', 'setting', 1, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_menu` VALUES (2, 1, '用户管理', 2, 'user:list', 'system/user/index', 'user', 'user', 1, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_menu` VALUES (3, 2, '新增', 3, 'user:add', NULL, NULL, NULL, 1, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_menu` VALUES (4, 2, '编辑', 3, 'user:edit', NULL, NULL, NULL, 2, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_menu` VALUES (5, 0, '社团管理', 1, NULL, NULL, '/club', 'team', 2, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_menu` VALUES (6, 5, '社团列表', 2, 'club:list', 'club/list', 'list', 'list', 1, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_menu` VALUES (7, 5, '活动管理', 2, 'activity:list', 'club/activity', 'activity', 'calendar', 2, 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码（如：CLUB_PRESIDENT）',
  `role_level` int NULL DEFAULT 0 COMMENT '角色等级（数字越小权限越高）',
  `data_scope` tinyint NULL DEFAULT NULL COMMENT '默认数据范围 1:全部 2:本学院 3:本社团 4:本部门 5:仅自己',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 0, 1, '系统管理员', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (2, '教师', 'advisor5', 4, 2, '教师', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (3, '老师', 'ADVISOR2', 0, 1, '指导老师', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (4, '老师', 'ADVISOR3', 2, 1, '普通老师', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (5, '社长', 'ADVISOR4', 2, 1, '社长', 1, '2026-07-04 11:52:21');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (2, 1, 2, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (3, 1, 3, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (4, 1, 4, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (5, 1, 5, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (6, 1, 6, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (7, 1, 7, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (8, 2, 5, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (9, 2, 6, '2026-02-11 21:48:57');
INSERT INTO `sys_role_menu` VALUES (10, 2, 7, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号（学号/工号）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `user_type` tinyint NOT NULL COMMENT '用户类型 1:学生 2:老师 3:管理员',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号（仅学生）',
  `teacher_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号（仅老师）',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号（敏感，仅少数人有权限查看）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 163 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户基础表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '玖', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '玖任羽', 2, '13383961145', 'jlz520@qq.com', '', 3, '', '20232015', '41170220050151132', 1, '2026-02-14 08:00:00', '2026-07-03 15:55:24');
INSERT INTO `sys_user` VALUES (40, '202320164602', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试学生', 1, '13254212315', NULL, '1', 1, NULL, NULL, NULL, 1, '2026-06-27 18:37:00', '2026-07-01 19:47:46');
INSERT INTO `sys_user` VALUES (41, '202320164604', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '玖祈愿', 0, NULL, NULL, '1', 1, NULL, NULL, NULL, 1, '2026-06-29 10:53:06', '2026-07-01 19:47:47');
INSERT INTO `sys_user` VALUES (42, '202310000001', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户1', 1, '13810000001', 'user1@test.com', '1', 1, '202310000001', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:48');
INSERT INTO `sys_user` VALUES (43, '202310000002', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户2', 2, '13810000002', 'user2@test.com', '1', 1, '202310000002', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:49');
INSERT INTO `sys_user` VALUES (44, '202310000003', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户3', 0, '13810000003', 'user3@test.com', '1', 1, '202310000003', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:49');
INSERT INTO `sys_user` VALUES (45, '202310000004', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户4', 1, '13810000004', 'user4@test.com', '1', 1, '202310000004', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:50');
INSERT INTO `sys_user` VALUES (46, '202310000005', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户5', 2, '13810000005', 'user5@test.com', '1', 1, '202310000005', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:51');
INSERT INTO `sys_user` VALUES (47, '202310000006', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户6', 0, '13810000006', 'user6@test.com', '1', 1, '202310000006', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:51');
INSERT INTO `sys_user` VALUES (48, '202310000007', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户7', 1, '13810000007', 'user7@test.com', '1', 1, '202310000007', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:52');
INSERT INTO `sys_user` VALUES (49, '202310000008', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户8', 2, '13810000008', 'user8@test.com', '1', 1, '202310000008', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:53');
INSERT INTO `sys_user` VALUES (50, '202310000009', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户9', 0, '13810000009', 'user9@test.com', '1', 1, '202310000009', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:54');
INSERT INTO `sys_user` VALUES (51, '202310000010', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户10', 1, '13810000010', 'user10@test.com', '1', 1, '202310000010', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:54');
INSERT INTO `sys_user` VALUES (52, '202310000011', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户11', 2, '13810000011', 'user11@test.com', '1', 1, '202310000011', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:55');
INSERT INTO `sys_user` VALUES (53, '202310000012', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户12', 0, '13810000012', 'user12@test.com', '1', 1, '202310000012', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:56');
INSERT INTO `sys_user` VALUES (54, '202310000013', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户13', 1, '13810000013', 'user13@test.com', '1', 1, '202310000013', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:56');
INSERT INTO `sys_user` VALUES (55, '202310000014', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户14', 2, '13810000014', 'user14@test.com', '1', 1, '202310000014', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:57');
INSERT INTO `sys_user` VALUES (56, '202310000015', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户15', 0, '13810000015', 'user15@test.com', '1', 1, '202310000015', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:47:58');
INSERT INTO `sys_user` VALUES (57, '202310000016', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户16', 1, '13810000016', 'user16@test.com', '1', 1, '202310000016', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:48:02');
INSERT INTO `sys_user` VALUES (58, '202310000017', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户17', 2, '13810000017', 'user17@test.com', '1', 1, '202310000017', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:48:00');
INSERT INTO `sys_user` VALUES (59, '202310000018', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户18', 0, '13810000018', 'user18@test.com', '1', 1, '202310000018', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:48:03');
INSERT INTO `sys_user` VALUES (60, '202310000019', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户19', 1, '13810000019', 'user19@test.com', '1', 1, '202310000019', NULL, NULL, 1, '2026-07-01 19:46:21', '2026-07-01 19:48:04');
INSERT INTO `sys_user` VALUES (61, '202310000020', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户20', 2, '13810000020', 'user20@test.com', NULL, 1, '202310000020', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (62, '202310000021', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户21', 0, '13810000021', 'user21@test.com', NULL, 1, '202310000021', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (63, '202310000022', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户22', 1, '13810000022', 'user22@test.com', NULL, 1, '202310000022', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (64, '202310000023', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户23', 2, '13810000023', 'user23@test.com', NULL, 1, '202310000023', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (66, '202310000025', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户25', 1, '13810000025', 'user25@test.com', NULL, 1, '202310000025', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (67, '202310000026', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户26', 2, '13810000026', 'user26@test.com', NULL, 1, '202310000026', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (68, '202310000027', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户27', 0, '13810000027', 'user27@test.com', NULL, 1, '202310000027', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (69, '202310000028', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户28', 1, '13810000028', 'user28@test.com', NULL, 1, '202310000028', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (70, '202310000029', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户29', 2, '13810000029', 'user29@test.com', NULL, 1, '202310000029', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (71, '202310000030', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户30', 0, '13810000030', 'user30@test.com', NULL, 1, '202310000030', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (72, '202310000031', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户31', 1, '13810000031', 'user31@test.com', NULL, 1, '202310000031', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (73, '202310000032', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户32', 2, '13810000032', 'user32@test.com', NULL, 1, '202310000032', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (74, '202310000033', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户33', 0, '13810000033', 'user33@test.com', NULL, 1, '202310000033', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (75, '202310000034', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户34', 1, '13810000034', 'user34@test.com', NULL, 1, '202310000034', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (76, '202310000035', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户35', 2, '13810000035', 'user35@test.com', NULL, 1, '202310000035', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (77, '202310000036', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户36', 0, '13810000036', 'user36@test.com', NULL, 1, '202310000036', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (78, '202310000037', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户37', 1, '13810000037', 'user37@test.com', NULL, 1, '202310000037', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (79, '202310000038', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户38', 2, '13810000038', 'user38@test.com', NULL, 1, '202310000038', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (80, '202310000039', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户39', 0, '13810000039', 'user39@test.com', NULL, 1, '202310000039', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (81, '202310000040', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户40', 1, '13810000040', 'user40@test.com', NULL, 1, '202310000040', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (82, '202310000041', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户41', 2, '13810000041', 'user41@test.com', NULL, 1, '202310000041', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (83, '202310000042', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户42', 0, '13810000042', 'user42@test.com', NULL, 1, '202310000042', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (84, '202310000043', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户43', 1, '13810000043', 'user43@test.com', NULL, 1, '202310000043', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (85, '202310000044', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户44', 2, '13810000044', 'user44@test.com', NULL, 1, '202310000044', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (86, '202310000045', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户45', 0, '13810000045', 'user45@test.com', NULL, 1, '202310000045', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (87, '202310000046', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户46', 1, '13810000046', 'user46@test.com', NULL, 1, '202310000046', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (88, '202310000047', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户47', 2, '13810000047', 'user47@test.com', NULL, 1, '202310000047', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (89, '202310000048', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户48', 0, '13810000048', 'user48@test.com', NULL, 1, '202310000048', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (90, '202310000049', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户49', 1, '13810000049', 'user49@test.com', NULL, 1, '202310000049', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (91, '202310000050', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户50', 2, '13810000050', 'user50@test.com', NULL, 1, '202310000050', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (92, '202310000051', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户51', 0, '13810000051', 'user51@test.com', NULL, 1, '202310000051', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (93, '202310000052', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户52', 1, '13810000052', 'user52@test.com', NULL, 1, '202310000052', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (94, '202310000053', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户53', 2, '13810000053', 'user53@test.com', NULL, 1, '202310000053', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (95, '202310000054', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户54', 0, '13810000054', 'user54@test.com', NULL, 1, '202310000054', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (96, '202310000055', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户55', 1, '13810000055', 'user55@test.com', NULL, 1, '202310000055', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (97, '202310000056', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户56', 2, '13810000056', 'user56@test.com', NULL, 1, '202310000056', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (98, '202310000057', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户57', 0, '13810000057', 'user57@test.com', NULL, 1, '202310000057', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (99, '202310000058', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户58', 1, '13810000058', 'user58@test.com', NULL, 1, '202310000058', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (100, '202310000059', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户59', 2, '13810000059', 'user59@test.com', NULL, 1, '202310000059', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (101, '202310000060', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户60', 0, '13810000060', 'user60@test.com', NULL, 1, '202310000060', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (102, '202310000061', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户61', 1, '13810000061', 'user61@test.com', NULL, 1, '202310000061', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (103, '202310000062', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户62', 2, '13810000062', 'user62@test.com', NULL, 1, '202310000062', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (104, '202310000063', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户63', 0, '13810000063', 'user63@test.com', NULL, 1, '202310000063', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (105, '202310000064', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户64', 1, '13810000064', 'user64@test.com', NULL, 1, '202310000064', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (106, '202310000065', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户65', 2, '13810000065', 'user65@test.com', NULL, 1, '202310000065', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (107, '202310000066', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户66', 0, '13810000066', 'user66@test.com', NULL, 1, '202310000066', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (108, '202310000067', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户67', 1, '13810000067', 'user67@test.com', NULL, 1, '202310000067', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (109, '202310000068', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户68', 2, '13810000068', 'user68@test.com', NULL, 1, '202310000068', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (110, '202310000069', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户69', 0, '13810000069', 'user69@test.com', NULL, 1, '202310000069', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (111, '202310000070', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户70', 1, '13810000070', 'user70@test.com', NULL, 1, '202310000070', NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (112, '1001', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户71', 2, '13810000071', 'user71@test.com', NULL, 2, NULL, '1001', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (113, '0002', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户72', 0, '13810000072', 'user72@test.com', NULL, 2, NULL, '0002', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (114, '1003', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户73', 1, '13810000073', 'user73@test.com', NULL, 2, NULL, '1003', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (115, '0004', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户74', 2, '13810000074', 'user74@test.com', NULL, 2, NULL, '0004', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (116, '1005', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户75', 0, '13810000075', 'user75@test.com', NULL, 2, NULL, '1005', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (117, '0006', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户76', 1, '13810000076', 'user76@test.com', NULL, 2, NULL, '0006', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (118, '1007', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户77', 2, '13810000077', 'user77@test.com', NULL, 2, NULL, '1007', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (119, '0008', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户78', 0, '13810000078', 'user78@test.com', NULL, 2, NULL, '0008', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (120, '1009', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户79', 1, '13810000079', 'user79@test.com', NULL, 2, NULL, '1009', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (121, '0010', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户80', 2, '13810000080', 'user80@test.com', NULL, 2, NULL, '0010', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (122, '1011', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户81', 0, '13810000081', 'user81@test.com', NULL, 2, NULL, '1011', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (123, '0012', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户82', 1, '13810000082', 'user82@test.com', NULL, 2, NULL, '0012', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (124, '1013', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户83', 2, '13810000083', 'user83@test.com', NULL, 2, NULL, '1013', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (125, '0014', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户84', 0, '13810000084', 'user84@test.com', NULL, 2, NULL, '0014', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (126, '1015', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户85', 1, '13810000085', 'user85@test.com', NULL, 2, NULL, '1015', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (127, '0016', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户86', 2, '13810000086', 'user86@test.com', NULL, 2, NULL, '0016', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (128, '1017', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户87', 0, '13810000087', 'user87@test.com', NULL, 2, NULL, '1017', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (129, '0018', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户88', 1, '13810000088', 'user88@test.com', NULL, 2, NULL, '0018', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (130, '1019', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户89', 2, '13810000089', 'user89@test.com', NULL, 2, NULL, '1019', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (131, '0020', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户90', 0, '13810000090', 'user90@test.com', NULL, 2, NULL, '0020', NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (132, 'admin01', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户91', 1, '13810000091', 'user91@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (133, 'admin02', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户92', 2, '13810000092', 'user92@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (134, 'admin03', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户93', 0, '13810000093', 'user93@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (135, 'admin04', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户94', 1, '13810000094', 'user94@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (136, 'admin05', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户95', 2, '13810000095', 'user95@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (137, 'admin06', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户96', 0, '13810000096', 'user96@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (138, 'admin07', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户97', 1, '13810000097', 'user97@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (139, 'admin08', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户98', 2, '13810000098', 'user98@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (140, 'admin09', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户99', 0, '13810000099', 'user99@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (141, 'admin10', '$2a$10$7dlzzr3nR3y8ms09sKubseH6a94b.hh6RXyYZp.fPQOYg7naW.Kuu', '测试用户100', 1, '13810000100', 'user100@test.com', NULL, 3, NULL, NULL, NULL, 1, '2026-07-01 19:46:21', NULL);
INSERT INTO `sys_user` VALUES (162, '问治涛', '$2a$10$vX8coPFvz/O96NjPRekFLOK4xq6KD9DD9sRMllp6IgXg0f7Tls1o2', '军倩', 1, '13854387604', 'k8qkfk.too89@sina.com', '', 1, '0275', '', '650102197612222319', 1, '2026-07-03 16:02:30', NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `scope_type` tinyint NULL DEFAULT NULL COMMENT '范围类型 1:学院 2:社团 3:部门 4:专业 5:班级',
  `scope_id` bigint NULL DEFAULT NULL COMMENT '具体范围ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role_scope`(`user_id` ASC, `role_id` ASC, `scope_type` ASC, `scope_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, 1, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (2, 1, 3, 3, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (3, 2, 3, 3, 2, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (5, 5, 1, NULL, NULL, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (8, 6, 4, 2, 2, '2026-06-29 10:55:50');
INSERT INTO `sys_user_role` VALUES (11, 41, 5, NULL, NULL, '2026-07-04 12:26:47');

SET FOREIGN_KEY_CHECKS = 1;
