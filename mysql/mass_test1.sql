/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80013 (8.0.13)
 Source Host           : localhost:3306
 Source Schema         : mass_test1

 Target Server Type    : MySQL
 Target Server Version : 80013 (8.0.13)
 File Encoding         : 65001

 Date: 15/02/2026 19:15:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_apply
-- ----------------------------
DROP TABLE IF EXISTS `activity_apply`;
CREATE TABLE `activity_apply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动编号',
  `club_id` bigint(20) NOT NULL COMMENT '主办社团',
  `activity_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '活动分类',
  `activity_type` tinyint(4) NULL DEFAULT NULL COMMENT '1:日常活动 2:比赛 3:演出 4:讲座 5:其他',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动地点',
  `location_detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细位置（用于定位）',
  `expected_people` int(11) NULL DEFAULT NULL COMMENT '预计参与人数',
  `budget` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算金额',
  `activity_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动内容',
  `safety_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '安全预案',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人',
  `apply_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `current_approve_step` int(11) NULL DEFAULT 0 COMMENT '当前审批步骤',
  `approve_status` tinyint(4) NULL DEFAULT 1 COMMENT '1:草稿 2:待审批 3:审批中 4:已通过 5:已驳回 6:已取消',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `activity_no`(`activity_no` ASC) USING BTREE,
  INDEX `idx_club_status`(`club_id` ASC, `approve_status` ASC) USING BTREE,
  INDEX `idx_apply_time`(`apply_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动申请表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) NOT NULL,
  `step` int(11) NOT NULL COMMENT '步骤序号',
  `approve_role_id` bigint(20) NULL DEFAULT NULL COMMENT '审批角色ID',
  `approve_user_id` bigint(20) NULL DEFAULT NULL COMMENT '实际审批人ID',
  `approve_result` tinyint(4) NULL DEFAULT NULL COMMENT '1:通过 2:驳回',
  `approve_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity`(`activity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动审批流程表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `approve_flow_id` bigint(20) NULL DEFAULT NULL COMMENT '审批流程模板ID',
  `need_location` tinyint(4) NULL DEFAULT 1 COMMENT '是否需要定位签到',
  `need_report` tinyint(4) NULL DEFAULT 0 COMMENT '是否需要活动报告',
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动分类表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `sign_type` tinyint(4) NULL DEFAULT NULL COMMENT '1:自动定位 2:手动签到 3:补签',
  `sign_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `sign_location` point NULL COMMENT '签到经纬度',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到地址文本',
  `sign_status` tinyint(4) NULL DEFAULT 1 COMMENT '1:正常 2:迟到 3:早退',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动签到表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_sign
-- ----------------------------
INSERT INTO `activity_sign` VALUES (1, 1, 2, 1, '2026-02-11 21:48:57', ST_GeomFromText('POINT(116.397 39.907)'), '教学楼101', 1, '2026-02-11 21:48:57');
INSERT INTO `activity_sign` VALUES (2, 1, 3, 1, '2026-02-11 21:48:57', ST_GeomFromText('POINT(116.397 39.907)'), '教学楼101', 1, '2026-02-11 21:48:57');
INSERT INTO `activity_sign` VALUES (3, 2, 1, 2, '2026-02-11 21:48:57', NULL, '阳光社区', 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for club_statistics
-- ----------------------------
DROP TABLE IF EXISTS `club_statistics`;
CREATE TABLE `club_statistics`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `club_id` bigint(20) NOT NULL,
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_members` int(11) NULL DEFAULT 0 COMMENT '当前总成员数',
  `new_members` int(11) NULL DEFAULT 0 COMMENT '新增成员数',
  `activity_count` int(11) NULL DEFAULT 0 COMMENT '活动次数',
  `total_participants` int(11) NULL DEFAULT 0 COMMENT '参与人次',
  `total_budget` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总预算',
  `avg_score` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '活动平均评分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_club_date`(`club_id` ASC, `stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社团统计表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `priority` tinyint(4) NULL DEFAULT 0 COMMENT '默认优先级',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知分类表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容（富文本）',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布人ID',
  `publisher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人姓名（冗余）',
  `importance` tinyint(4) NULL DEFAULT 1 COMMENT '重要程度 1:低 2:中 3:高',
  `urgency` tinyint(4) NULL DEFAULT 1 COMMENT '紧急程度 1:不紧急 2:紧急',
  `receiver_type` tinyint(4) NOT NULL COMMENT '接收类型 1:全体学生 2:全体老师 3:指定角色 4:指定社团 5:指定人员',
  `receiver_values` json NULL COMMENT '接收者值（角色ID/社团ID/用户ID列表）',
  `need_confirm` tinyint(4) NULL DEFAULT 0 COMMENT '是否需要确认阅读',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '0:草稿 1:已发布 2:已撤回',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `notice_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `read_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间（如果need_confirm=1）',
  `is_confirmed` tinyint(4) NULL DEFAULT 0 COMMENT '是否已确认',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知阅读记录表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `club_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '社团名称',
  `club_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团编号',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团类别（学术科技/文化体育/公益等）',
  `college_id` bigint(20) NULL DEFAULT NULL COMMENT '挂靠学院（可为空）',
  `advisor_id` bigint(20) NULL DEFAULT NULL COMMENT '指导老师ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '社团简介',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社团logo',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态 0:解散 1:正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `club_code`(`club_code` ASC) USING BTREE,
  INDEX `idx_college`(`college_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社团表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `college_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学院名称',
  `college_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院代码',
  `dean_id` bigint(20) NULL DEFAULT NULL COMMENT '院长ID（关联sys_user）',
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `college_code`(`college_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学院表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `table_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '表名',
  `field_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段名',
  `visible` tinyint(4) NULL DEFAULT 1 COMMENT '是否可见 0:隐藏 1:可见 2:只读',
  `condition_type` tinyint(4) NULL DEFAULT NULL COMMENT '行级条件 1:全部 2:本部门 3:本人',
  `condition_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义条件',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据权限规则表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `club_id` bigint(20) NOT NULL COMMENT '所属社团',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门ID（用于部门层级）',
  `leader_id` bigint(20) NULL DEFAULT NULL COMMENT '部长ID',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门职责',
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_club`(`club_id` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社团部门表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '专业名称',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业代码',
  `college_id` bigint(20) NOT NULL COMMENT '所属学院',
  `head_teacher_id` bigint(20) NULL DEFAULT NULL COMMENT '专业负责人ID',
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `major_code`(`major_code` ASC) USING BTREE,
  INDEX `idx_college`(`college_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专业表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint(4) NOT NULL COMMENT '类型 1:目录 2:菜单 3:按钮',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识（如 user:list, activity:approve）',
  `component_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径',
  `route_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_permission`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码（如：CLUB_PRESIDENT）',
  `role_level` int(11) NULL DEFAULT 0 COMMENT '角色等级（数字越小权限越高）',
  `data_scope` tinyint(4) NULL DEFAULT NULL COMMENT '默认数据范围 1:全部 2:本学院 3:本社团 4:本部门 5:仅自己',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 0, 1, '系统管理员', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (2, '社团社长', 'CLUB_PRESIDENT', 10, 3, '社团负责人', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (3, '社团部长', 'CLUB_MINISTER', 20, 4, '部门负责人', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (4, '普通成员', 'MEMBER', 99, 5, '社团普通成员', 1, '2026-02-11 21:48:57');
INSERT INTO `sys_role` VALUES (5, '指导老师', 'ADVISOR', 15, 3, '社团指导老师', 1, '2026-02-11 21:48:57');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号（学号/工号）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `gender` tinyint(4) NULL DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `user_type` tinyint(4) NOT NULL COMMENT '用户类型 1:学生 2:老师 3:管理员',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号（仅学生）',
  `teacher_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号（仅老师）',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号（敏感，仅少数人有权限查看）',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户基础表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '玖', '123456', '玖任羽', 0, '13383961145', 'jlz520@qq.com', '', 1, '20232015', '', '41170220050151132', 0, '2026-02-14 08:00:00', '2026-02-14 15:02:25');
INSERT INTO `sys_user` VALUES (2, '2022002', '123456', '李四', 2, '13800138002', 'lisi@example.com', NULL, 1, '2022002', NULL, NULL, 1, '2026-02-11 21:48:57', NULL);
INSERT INTO `sys_user` VALUES (4, 'T1002', '123456', '赵教授', 2, '13800138004', 'zhao@example.com', NULL, 2, NULL, 'T1002', NULL, 1, '2026-02-11 21:48:57', NULL);
INSERT INTO `sys_user` VALUES (5, 'admin', '123456', '管理员', 0, NULL, 'admin@example.com', NULL, 3, NULL, NULL, NULL, 1, '2026-02-11 21:48:57', NULL);
INSERT INTO `sys_user` VALUES (6, 'Ota Yota', 'GdEfmuk5XC', 'Ota Yota', 78, '213-267-5784', 'yotao5@icloud.com', 'Zr2JJHMoJu', 88, '4czJ5fkp1Z', 'KljMtZi7Q5', 'gKaBzmzbzW', 6, '2022-05-30 11:33:17', '2018-04-15 16:04:33');
INSERT INTO `sys_user` VALUES (7, 'Vincent Bennett', 'AOa9slOn1a', 'Vincent Bennett', 14, '80-4058-6794', 'bennett712@mail.com', 'X5lJCwOOnl', 14, '2LndG3TJNA', '9Ld6WFQRJW', 'mzHSYVWm4H', 31, '2013-08-22 11:58:24', '2001-06-30 14:47:19');
INSERT INTO `sys_user` VALUES (8, 'Wayne Wood', '8Rcqds7xup', 'Wayne Wood', 119, '90-7342-7745', 'woow508@yahoo.com', 'YqZq5YtSaL', 72, 'EI2hJPkPUp', 'BkgzMUc6zn', 'LbiBHhib4J', 72, '2025-03-24 13:36:11', '2005-04-18 04:10:04');
INSERT INTO `sys_user` VALUES (9, 'Antonio Young', 'MFAmWxKQP9', 'Antonio Young', 54, '(20) 5958 5455', 'antonioyoung@icloud.com', '4POdoIdcnE', 29, 'ANahl5IeR3', 'YH3Y6VQa3I', 'qWkhylDOF7', 87, '2007-03-21 23:35:09', '2016-12-22 11:14:54');
INSERT INTO `sys_user` VALUES (10, 'Li Rui', 'yi7voo67XS', 'Li Rui', 121, '66-830-7977', 'rli@mail.com', '0EkWJfOJxe', 82, 'z4kvNVpqHr', 'TCGvQj1RTi', 'z5WVHLiYNZ', 124, '2004-07-17 00:18:31', '2012-11-14 13:06:57');
INSERT INTO `sys_user` VALUES (11, 'Cui Lu', 'EHzmNGqpVy', 'Cui Lu', 31, '28-7552-8726', 'cuilu@gmail.com', 'YJ1zGGlukt', 100, 'uLrKkLXqQV', 'cXrdM3VlKL', 'HgOhqvLY1h', 48, '2023-09-14 01:39:45', '2016-09-14 03:07:39');
INSERT INTO `sys_user` VALUES (12, 'Shibata Yota', 'tr4H0QiD4W', 'Shibata Yota', 83, '52-618-8957', 'yotashibata8@mail.com', '9XxXWEzbCD', 43, '6dx5VbeaVC', 'gIhJMBEXrD', '0fYQV3GOp8', 65, '2009-01-15 23:37:14', '2020-07-31 20:37:31');
INSERT INTO `sys_user` VALUES (13, 'Lok Wai Man', 'sJlwuzJRjX', 'Lok Wai Man', 97, '5801 192411', 'lowm@hotmail.com', 'mSj258IP2Y', 126, 'DNIZmCdaax', 'dOUqAm6JRD', '1r2nuRkaZZ', 110, '2024-08-20 05:25:50', '2023-03-14 22:35:38');
INSERT INTO `sys_user` VALUES (14, 'Clarence Guzman', 'cmd14rwe9d', 'Clarence Guzman', 116, '7925 810000', 'guzman6@hotmail.com', 'tzM1SiCviD', 12, 'lHypuF47hw', 'da6P0Toyu1', 'YK5mfRFlHK', 102, '2010-08-11 21:50:56', '2003-03-01 13:49:51');
INSERT INTO `sys_user` VALUES (15, 'Phillip Edwards', 'puLXE84RKG', 'Phillip Edwards', 2, '169-3412-0940', 'edphi327@outlook.com', 'ltENSXlXJ0', 14, 'mB16Qwalh5', 'zmRtGwNLuU', 'rkp7DD5gCk', 116, '2008-05-15 08:25:04', '2023-10-25 12:36:34');
INSERT INTO `sys_user` VALUES (16, 'Nicholas Murphy', 'RguQhQqXra', 'Nicholas Murphy', 64, '(116) 652 9431', 'murnic7@outlook.com', '7c1kMpg6kb', 125, 'ofC3Ukp6lv', '8zvzgrRUhT', 'W7JkhyivdI', 117, '2002-12-23 12:18:45', '2022-06-24 03:23:01');
INSERT INTO `sys_user` VALUES (17, 'Katherine Herrera', 'nPo8aTm5MR', 'Katherine Herrera', 43, '10-951-2472', 'katherineherrera@gmail.com', 'KN9rlls28f', 118, '1PfFLUv1RF', 'FeK1TqYYby', 'MjNnDhTCFX', 92, '2024-09-10 17:28:36', '2017-01-09 20:44:50');
INSERT INTO `sys_user` VALUES (18, 'Han Chun Yu', 'LfNNW0XJ9i', 'Han Chun Yu', 10, '755-384-5928', 'hanchunyu@gmail.com', '1y2yUm5bn0', 58, 'InItvXmf0H', 'gZJjhLY4Fd', '9WKWox9J3B', 13, '2025-01-11 09:45:56', '2022-10-12 01:43:37');
INSERT INTO `sys_user` VALUES (19, 'Li Lu', 'E2pxNftBX3', 'Li Lu', 91, '21-2768-3426', 'lu10@outlook.com', 'imOc3OypmO', 84, 'IJ6N8yXoGb', 'E5lil2X2LK', 'mjZ7iCzxj4', 125, '2017-03-28 14:52:30', '2025-12-08 13:54:08');
INSERT INTO `sys_user` VALUES (20, 'Yin Ka Ming', 'CEdLD9ISyt', 'Yin Ka Ming', 48, '154-9736-4778', 'kmyin46@gmail.com', 'T8F5RwVHXr', 33, 'VL641WvIkn', 'GdsSG7r3NU', 'Jrc12z2xbe', 76, '2012-09-06 12:38:06', '2011-08-04 17:10:01');
INSERT INTO `sys_user` VALUES (21, 'Rosa Mitchell', 'sCkqCL7m7j', 'Rosa Mitchell', 32, '164-6538-6074', 'mitchellrosa67@icloud.com', 'qVDTvF0bUa', 79, 'IRnmh9zmkw', 'EXl25tNv8b', 'pheHazY7L8', 106, '2014-05-08 20:04:19', '2020-02-08 08:18:44');
INSERT INTO `sys_user` VALUES (22, 'David Daniels', 'FyLKJugN1o', 'David Daniels', 24, '718-269-7493', 'dadanie2@outlook.com', 'smqa20M0H7', 108, 'EL5vWIMN9D', 'p5zmbO4MsK', 'BgwofVwJ3h', 57, '2016-11-22 21:05:10', '2022-01-16 08:10:03');
INSERT INTO `sys_user` VALUES (23, 'Eddie Thompson', 'VPkpPVHZpC', 'Eddie Thompson', 63, '718-556-2854', 'eddie77@outlook.com', 'FOYTwHXQ94', 31, 'fdAhv4q3nJ', '2DRZzqGuv4', 'PsCS5XjMJW', 25, '2022-07-31 19:28:53', '2003-06-03 00:17:18');
INSERT INTO `sys_user` VALUES (24, 'Sano Yuito', 'ouRYcVXIl2', 'Sano Yuito', 109, '(20) 1469 8091', 'sanoyuito816@icloud.com', 'TIZdqtyDi9', 30, 'JeMhfxRN8W', '7uAYbHqp6b', 'HWLimp6ruy', 114, '2024-09-15 07:09:29', '2024-08-27 04:17:18');
INSERT INTO `sys_user` VALUES (25, 'Mui On Kay', 'gl8mLVQu6v', 'Mui On Kay', 70, '5796 081874', 'okmui@hotmail.com', 'g9aGwukcmh', 15, '8IEl6By6wB', 'xP5p1UPgrx', 'Lw7U4zRXCk', 24, '2014-02-16 11:59:08', '2006-05-13 02:51:21');
INSERT INTO `sys_user` VALUES (26, 'Mildred Weaver', '3Un3eRIZC7', 'Mildred Weaver', 33, '140-9282-2657', 'weaver4@gmail.com', 'wrsZvSCHbI', 75, 'meJKQqYsOb', 'YgU5p9zgzJ', 'nowzzPK0AX', 68, '2004-01-05 20:34:19', '2021-10-21 02:04:08');
INSERT INTO `sys_user` VALUES (27, 'Juanita Lewis', 'gfEJBeD5Yv', 'Juanita Lewis', 62, '180-2995-9292', 'juanitalewis@outlook.com', 'D9llNuDUSw', 11, 'HndhjsIBaR', 'CCE52h4mfk', 'oSPIKnSTof', 84, '2011-10-29 17:34:02', '2014-06-14 07:00:43');
INSERT INTO `sys_user` VALUES (28, 'Koo Chi Yuen', 'AfEXRLFQeX', 'Koo Chi Yuen', 119, '838-444-2365', 'koocy63@hotmail.com', '055uZEbPlW', 104, 'PSDmwss4zK', 'SG0yyROpf3', 'YREFlw0Zu8', 56, '2016-02-01 14:54:56', '2008-06-23 10:07:27');
INSERT INTO `sys_user` VALUES (29, 'Lui Wai Han', 'h4WLFpPodu', 'Lui Wai Han', 113, '20-197-1731', 'luiwh508@icloud.com', '1DlmABKc9N', 26, 'bTyyAQ0IyS', 'Dnj71Mn6Ay', '127mlUmNO7', 26, '2008-10-22 10:52:36', '2008-10-08 16:27:54');
INSERT INTO `sys_user` VALUES (30, 'Rosa Gibson', 'Ri1kftiDQ7', 'Rosa Gibson', 35, '11-195-6787', 'rosagibs@gmail.com', '6eug6uW9XQ', 62, 'Rq2jBdf9vT', 'erFl723Q9e', 'tuw9a4QypX', 104, '2024-03-19 17:28:48', '2006-11-11 14:15:09');
INSERT INTO `sys_user` VALUES (31, 'Endo Mai', 'GJkKRBYceP', 'Endo Mai', 82, '838-515-0612', 'maendo@outlook.com', 'GletEGYJQz', 91, 'DA62OaKDjV', 'cD2dzIhmwH', 'wHtp25HxIA', 79, '2003-02-09 15:35:10', '2003-04-25 01:17:07');
INSERT INTO `sys_user` VALUES (32, 'Tamura Shino', 'GV56AxDw7U', 'Tamura Shino', 21, '180-1253-4379', 'tshino@outlook.com', 'BBP33rwVuz', 5, '0DaTH4Vifc', 'ZBIi17wbxr', 'vNakklKifo', 116, '2018-11-10 01:24:50', '2016-05-28 07:16:44');
INSERT INTO `sys_user` VALUES (33, 'Norma Turner', '4pBZ8BEORK', 'Norma Turner', 106, '(161) 574 6422', 'turnerno99@mail.com', 'ESUWAh2AC1', 65, '6t3tQRskDB', 'brbRcbJxpo', 'DM1fkxx3Ag', 20, '2007-05-07 07:50:30', '2001-12-01 22:32:09');
INSERT INTO `sys_user` VALUES (34, 'Luis Chen', 'no20FZ1Dpc', 'Luis Chen', 115, '838-101-0585', 'luchen@mail.com', '69RfwkKuL1', 47, 'kqsuUIOF1w', 'O7bmXFqGZB', 'bBEqxpGy6D', 67, '2008-08-02 11:46:17', '2001-07-31 12:04:42');
INSERT INTO `sys_user` VALUES (35, 'Hirano Takuya', 'j2JgP1PLfx', 'Hirano Takuya', 102, '614-706-6701', 'hitakuya@yahoo.com', 'VEYIH0tzbG', 80, 'LCQoi8tCjs', '3xMrid4Qji', 'l21DyDZqbk', 107, '2023-11-30 22:27:18', '2010-12-28 09:20:23');
INSERT INTO `sys_user` VALUES (36, 'Sato Rin', 'FTLtQhULXY', 'Sato Rin', 105, '5990 344630', 'satorin@icloud.com', 'xoHSssgukV', 99, 'qUxqALfSVC', 'VWU7HZVJRS', 'JaWkkkFy6g', 58, '2016-03-09 19:42:40', '2010-02-02 11:21:45');
INSERT INTO `sys_user` VALUES (37, 'Sherry Murray', 'vR5QN8lFp6', 'Sherry Murray', 66, '80-6692-7350', 'sherrym@icloud.com', 'xWlg3zh5nF', 122, 'NK2b1K10Zk', 'gE0fEZdZf3', 'J17EoKxfQB', 117, '2005-07-30 00:44:30', '2008-06-24 17:20:37');
INSERT INTO `sys_user` VALUES (38, 'Wang Xiuying', 'Kb9nUhMWL2', 'Wang Xiuying', 32, '66-770-2640', 'wax@mail.com', '3uJ6jBY5gv', 117, 'o0D2o8Cnas', 'gvcYdsMpeG', 'TnuWxXcqzO', 117, '2013-07-09 15:51:48', '2015-06-27 02:30:54');
INSERT INTO `sys_user` VALUES (39, 'Ueda Daichi', 'JJcedcbGK8', 'Ueda Daichi', 89, '21-5170-8218', 'uedadaichi@icloud.com', '4cvafa740o', 53, 'PBeBgaHHmt', 'Fh3ffcLdur', 'qMUQcxrwBs', 66, '2005-09-22 15:10:31', '2016-01-16 01:13:24');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `scope_type` tinyint(4) NULL DEFAULT NULL COMMENT '范围类型 1:学院 2:社团 3:部门 4:专业 5:班级',
  `scope_id` bigint(20) NULL DEFAULT NULL COMMENT '具体范围ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role_scope`(`user_id` ASC, `role_id` ASC, `scope_type` ASC, `scope_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 2, 2, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (2, 1, 3, 3, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (3, 2, 3, 3, 2, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (4, 3, 5, 2, 1, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (5, 5, 1, NULL, NULL, '2026-02-11 21:48:57');
INSERT INTO `sys_user_role` VALUES (6, 4, 5, 2, 2, '2026-02-11 21:48:57');

-- ----------------------------
-- 后置说明（可选）
-- 种子数据中 password 为明文，Spring Security 使用 BCryptPasswordEncoder。
-- 导入后请通过 POST /register/single 注册新用户，或执行 mysql/fix_bcrypt_passwords.sql
-- （需先用 BCryptPasswordEncoder.encode("123456") 生成哈希后更新该脚本）
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
