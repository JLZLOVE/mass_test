package untiy.exception;

public class ErrorConfig {

    // ====================== 通用 / HTTP (4xx) ======================
    public static final Integer BAD_REQUEST_CODE = Usual.BAD_REQUEST_CODE;
    public static final String BAD_REQUEST_MSG = Usual.BAD_REQUEST_MSG;

    // ====================== 用户模块 (1xxx) ======================
    // 注册 / 用户基础信息
    public static final Integer RGEISTER_ADD_NEW_USER_CODE = 1001;
    public static final String RGEISTER_ADD_NEW_USER_MSG = "注册失败,请重新尝试";

    public static final Integer RGEISTER_PASSWORD_OR_NAMEC_CODE = 1002;
    public static final String RGEISTER_PASSWORD_OR_NAMEC_MSG = "密码或姓名为空,请重新输入";

    public static final Integer RGEISTER_STATUS_CODE = 1003;
    public static final String RGEISTER_STATUS_CODE_MSG = "用户不存在,请重新输入学号或工号";

    public static final Integer RGEISTER_PASSWORD_CODE = 1004;
    public static final String RGEISTER_PASSWORD_MSG = "密码不匹配,请重新输入";

    public static final Integer Author_ID_CODE = 1005;
    public static final String Author_ID_MSG = "用户序号为空,异常警告";

    public static final Integer USERNAME_EXIST_CODE = 1006;
    public static final String USERNAME_EXIST_MSG = "用户名已存在";

    public static final Integer USER_EMPTY_CODE = 1007;
    public static final String USER_EMPTY_MSG = "用户实体为空";

    public static final Integer USERNAME_BLANK_CODE = 1008;
    public static final String USERNAME_BLANK_MSG = "用户名为空";

    // ⚠️ 原 0002（无权删除用户）迁移至此
    public static final Integer NO_PERM_DELETE_USER_CODE = 1009;
    public static final String NO_PERM_DELETE_USER_MSG = "无权改变用户: ";

    // ====================== 通用校验 (7xxx) ======================
    public static final Integer ILLEGAL_Character_CODE = 7001;
    public static final String ILLEGAL_Character_MSG = "包含非法字符";

    // ====================== 认证 / Token (8xxx) ======================
    // ⚠️ 重复定义警告：LOGIN_INVALID (8001) 与 NOT_LOGGED_IN (8001) 含义完全一致，请合并
    public static final Integer LOGIN_INVALID_CODE = 8001;
    public static final String LOGIN_INVALID_MSG = "未登录或会话已失效";

    /** 与 {@link #LOGIN_INVALID_CODE} 同义，兼容旧引用 */
    public static final Integer NOT_LOGGED_IN_CODE = LOGIN_INVALID_CODE;
    public static final String NOT_LOGGED_IN_MSG = LOGIN_INVALID_MSG;
    public static final Integer TOKEN_ERROR_CODE = 8002; // 重复，建议删除此项
    public static final String TOKEN_ERROR_MSG = "token解析异常";

    public static final Integer TOKEN_EXPIRED_CODE = 8003;
    public static final String TOKEN_EXPIRED_MSG = "token已过期";

    public static final Integer TOKEN_MISSING = 8004;
    public static final String TOKEN_MISSING_MSG = "请求中未携带token";

    // ====================== 活动模块 (6xxx) ======================
    public static final Integer ACT_CATEGORYID_NULL_CODE = 6001;
    public static final String ACT_CATEGORYID_NULL_MSG = "该活动后缀为空";

    public static final Integer ACT_CONTENT_NULL_CODE = 6002;
    public static final String ACT_CONTENT_NULL_MSG = "活动分类id为空或";

    public static final Integer ACT_NO_NULL_CODE = 6003;
    public static final String ACT_NO_NULL_MSG = "活动编号为空";

    // ====================== 角色权限 (9xxx) ======================
    public static final Integer ROLE_NOT_FOUND_CODE = 9001;
    public static final String ROLE_NOT_FOUND_MSG = "角色不存在";

    public static final Integer ROLE_LEVEL_INSUFFICIENT_CODE = 9002;
    public static final String ROLE_LEVEL_INSUFFICIENT_MSG = "无权创建高于自身等级的角色";

    public static final Integer ROLE_CANNOT_MODIFY_HIGHER_CODE = 9003;
    public static final String ROLE_CANNOT_MODIFY_HIGHER_MSG = "不能修改更高等级的角色";

    public static final Integer ROLE_CANNOT_ELEVATE_CODE = 9004;
    public static final String ROLE_CANNOT_ELEVATE_MSG = "不能提升自身权限等级";

    public static final Integer ROLE_IN_USE_CODE = 9005;
    public static final String ROLE_IN_USE_MSG = "不能删除当前用户正在使用的角色";

    public static final Integer ROLE_NO_PERMISSION_CODE = 9006;
    public static final String ROLE_NO_PERMISSION_MSG = "无权查看该角色";

    public static final Integer ROLE_CODE_BLANK_CODE = 9007;
    public static final String ROLE_CODE_BLANK_MSG = "角色编码不能为空";

    public static final Integer USER_DISABLED_CODE = 9008;
    public static final String USER_DISABLED_MSG = "用户已被禁用，无法操作";

    public static final Integer CANNOT_DISABLE_SELF_CODE = 9009;
    public static final String CANNOT_DISABLE_SELF_MSG = "不能禁用当前登录账号";

    public static final Integer BATCH_CONTAINS_DISABLED_CODE = 9010;
    public static final String BATCH_CONTAINS_DISABLED_MSG = "批量操作中包含已禁用用户，已拒绝";

    public static final Integer ROLE_ASSIGN_DUPLICATE_CODE = 9011;
    public static final String ROLE_ASSIGN_DUPLICATE_MSG = "角色分配冲突：已存在相同或互斥的范围记录";

    public static final Integer ROLE_SCOPE_INVALID_CODE = 9012;
    public static final String ROLE_SCOPE_INVALID_MSG = "角色数据范围不合法";

    public static final Integer ROLE_REVOKE_SELF_CODE = 9013;
    public static final String ROLE_REVOKE_SELF_MSG = "不能撤销自己当前持有的角色";

    public static final Integer USER_ROLE_NOT_FOUND_CODE = 9014;
    public static final String USER_ROLE_NOT_FOUND_MSG = "用户角色关联不存在";

    // ====================== 菜单管理 (91xx) ======================
    public static final Integer MENU_NOT_FOUND_CODE = 9101;
    public static final String MENU_NOT_FOUND_MSG = "菜单不存在";

    public static final Integer MENU_PARENT_NOT_FOUND_CODE = 9102;
    public static final String MENU_PARENT_NOT_FOUND_MSG = "父菜单不存在";

    public static final Integer MENU_CYCLE_CODE = 9103;
    public static final String MENU_CYCLE_MSG = "不能将菜单挂到自身或子级下";

    public static final Integer MENU_NAME_DUPLICATE_CODE = 9104;
    public static final String MENU_NAME_DUPLICATE_MSG = "同级菜单名称已存在";

    public static final Integer MENU_HAS_CHILDREN_CODE = 9105;
    public static final String MENU_HAS_CHILDREN_MSG = "存在子菜单，无法删除";

    public static final Integer MENU_BOUND_HIGHER_ROLE_CODE = 9106;
    public static final String MENU_BOUND_HIGHER_ROLE_MSG = "该菜单已被更高权限角色绑定，无法操作";

    public static final Integer MENU_COMPONENT_REQUIRED_CODE = 9107;
    public static final String MENU_COMPONENT_REQUIRED_MSG = "菜单类型为页面时，组件路径不能为空";

    public static final Integer MENU_IDS_INVALID_CODE = 9108;
    public static final String MENU_IDS_INVALID_MSG = "部分菜单不存在或已失效";

    // ====================== 社团申请 / 合议 (92xx) ======================
    public static final Integer CLUB_APPLY_NOT_FOUND_CODE = 9201;
    public static final String CLUB_APPLY_NOT_FOUND_MSG = "社团申请不存在";
    public static final Integer CLUB_NOT_FOUND_CODE = 9202;
    public static final String CLUB_NOT_FOUND_MSG = "社团不存在";
    public static final Integer CLUB_NOT_NORMAL_CODE = 9203;
    public static final String CLUB_NOT_NORMAL_MSG = "社团状态异常，无法操作";
    public static final Integer CLUB_NAME_DUPLICATE_CODE = 9204;
    public static final String CLUB_NAME_DUPLICATE_MSG = "同一学院下社团名称已存在";
    public static final Integer CLUB_NOT_ADVISOR_CODE = 9205;
    public static final String CLUB_NOT_ADVISOR_MSG = "当前用户不是该社团指导老师";
    public static final Integer CLUB_HAS_ACTIVE_ACTIVITY_CODE = 9206;
    public static final String CLUB_HAS_ACTIVE_ACTIVITY_MSG = "社团存在进行中的活动，无法解散";
    public static final Integer CLUB_APPLY_STATUS_INVALID_CODE = 9207;
    public static final String CLUB_APPLY_STATUS_INVALID_MSG = "申请状态不允许此操作";
    public static final Integer CLUB_NOT_DEAN_CODE = 9208;
    public static final String CLUB_NOT_DEAN_MSG = "仅学院负责人可审批该申请";
    public static final Integer CLUB_CANNOT_APPROVE_SELF_CODE = 9209;
    public static final String CLUB_CANNOT_APPROVE_SELF_MSG = "不能审批自己提交的申请";
    public static final Integer CLUB_COUNCIL_NOT_FOUND_CODE = 9210;
    public static final String CLUB_COUNCIL_NOT_FOUND_MSG = "合议记录不存在";
    public static final Integer CLUB_COUNCIL_IN_PROGRESS_CODE = 9211;
    public static final String CLUB_COUNCIL_IN_PROGRESS_MSG = "该社团已有进行中的合议";
    public static final Integer CLUB_COUNCIL_NOT_IN_PROGRESS_CODE = 9212;
    public static final String CLUB_COUNCIL_NOT_IN_PROGRESS_MSG = "合议不在进行中";
    public static final Integer CLUB_ALREADY_SIGNED_CODE = 9213;
    public static final String CLUB_ALREADY_SIGNED_MSG = "您已签字，不能重复签字";
    public static final Integer CLUB_COLLEGE_OUT_OF_SCOPE_CODE = 9214;
    public static final String CLUB_COLLEGE_OUT_OF_SCOPE_MSG = "不在该学院管理范围内";
    public static final Integer CLUB_ROLE_NOT_FOUND_CODE = 9215;
    public static final String CLUB_ROLE_NOT_FOUND_MSG = "系统角色配置缺失";
    public static final Integer CLUB_PROPOSED_LEADER_INVALID_CODE = 9216;
    public static final String CLUB_PROPOSED_LEADER_INVALID_MSG = "拟定社长不存在或已被禁用";
    public static final Integer CLUB_REQUIRE_ADVISOR_ROLE_CODE = 9217;
    public static final String CLUB_REQUIRE_ADVISOR_ROLE_MSG = "需要指导老师角色";
    public static final Integer CLUB_REQUIRE_ADMIN_ROLE_CODE = 9218;
    public static final String CLUB_REQUIRE_ADMIN_ROLE_MSG = "需要校级管理员权限";

    // ====================== 活动审批 (93xx) ======================
    public static final Integer ACT_APPLY_NOT_FOUND_CODE = 9301;
    public static final String ACT_APPLY_NOT_FOUND_MSG = "活动申请不存在";
    public static final Integer ACT_CLUB_NOT_FOUND_CODE = 9302;
    public static final String ACT_CLUB_NOT_FOUND_MSG = "主办社团不存在或状态异常";
    public static final Integer ACT_SUBMIT_NO_PERMISSION_CODE = 9303;
    public static final String ACT_SUBMIT_NO_PERMISSION_MSG = "当前用户无权发起活动申请";
    public static final Integer ACT_LEVEL_INVALID_CODE = 9304;
    public static final String ACT_LEVEL_INVALID_MSG = "活动级别必须为院级或校级";
    public static final Integer ACT_STATUS_INVALID_CODE = 9305;
    public static final String ACT_STATUS_INVALID_MSG = "活动状态不允许此操作";
    public static final Integer ACT_NOT_CURRENT_APPROVER_CODE = 9306;
    public static final String ACT_NOT_CURRENT_APPROVER_MSG = "您不是当前步骤审批人";
    public static final Integer ACT_OPINION_REQUIRED_CODE = 9307;
    public static final String ACT_OPINION_REQUIRED_MSG = "审批意见不能为空";
    public static final Integer ACT_VERSION_CONFLICT_CODE = 9308;
    public static final String ACT_VERSION_CONFLICT_MSG = "数据已被修改，请刷新重试";
    public static final Integer ACT_APPROVER_NOT_FOUND_CODE = 9309;
    public static final String ACT_APPROVER_NOT_FOUND_MSG = "无法确定审批人，请检查角色配置";
    public static final Integer ACT_NOT_APPLICANT_CODE = 9310;
    public static final String ACT_NOT_APPLICANT_MSG = "仅申请人可执行此操作";
    public static final Integer ACT_CHANGE_FIELDS_INVALID_CODE = 9311;
    public static final String ACT_CHANGE_FIELDS_INVALID_MSG = "变更仅允许修改时间或地点";
    public static final Integer ACT_SUMMARY_WINDOW_CODE = 9312;
    public static final String ACT_SUMMARY_WINDOW_MSG = "活动总结须在结束后1-3天内上传";
    public static final Integer ACT_TIME_INVALID_CODE = 9313;
    public static final String ACT_TIME_INVALID_MSG = "开始时间必须早于结束时间";
    public static final Integer ACT_LEVEL_ADJUST_LOCKED_CODE = 9314;
    public static final String ACT_LEVEL_ADJUST_LOCKED_MSG = "活动级别已调整过，不可再次修改";
}
