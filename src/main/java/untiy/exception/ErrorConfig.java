package untiy.exception;

public class ErrorConfig {

    // 未登录或会话已失效
    public static final Integer LOGIN_INVALID_CODE = 001;
    public static final String LOGIN_INVALID_MSG = "未登录或会话已失效";
    //    注册时 ,后台添加不上
    public static final Integer RGEISTER_ADD_NEW_USER_CODE = 1001;
    public static final String RGEISTER_ADD_NEW_USER_MSG = "注册失败,请重新尝试";
//    ======================角色============================
    public static final Integer ROLE_NOT_FOUND_CODE = 5001;
    public static final String ROLE_NOT_FOUND_MSG = "角色不存在";
    public static final Integer ROLE_LEVEL_INSUFFICIENT_CODE = 5002;
    public static final String ROLE_LEVEL_INSUFFICIENT_MSG = "无权创建高于自身等级的角色";
    public static final Integer ROLE_CANNOT_MODIFY_HIGHER_CODE = 5003;
    public static final String ROLE_CANNOT_MODIFY_HIGHER_MSG = "不能修改更高等级的角色";
    public static final Integer ROLE_CANNOT_ELEVATE_CODE = 5004;
    public static final String ROLE_CANNOT_ELEVATE_MSG = "不能提升自身权限等级";
    public static final Integer ROLE_IN_USE_CODE = 5005;
    public static final String ROLE_IN_USE_MSG = "不能删除当前用户正在使用的角色";
    public static final Integer ROLE_NO_PERMISSION_CODE = 5006;
    public static final String ROLE_NO_PERMISSION_MSG = "无权查看该角色";
    public static final Integer ROLE_CODE_BLANK_CODE = 5007;
    public static final String ROLE_CODE_BLANK_MSG = "角色编码不能为空";
    public static final Integer BAD_REQUEST_CODE = 400;
    public static final String BAD_REQUEST_MSG = "请求参数无效";
    public static final Integer USER_DISABLED_CODE = 5008;
    public static final String USER_DISABLED_MSG = "用户已被禁用，无法操作";
    public static final Integer CANNOT_DISABLE_SELF_CODE = 5009;
    public static final String CANNOT_DISABLE_SELF_MSG = "不能禁用当前登录账号";
    public static final Integer BATCH_CONTAINS_DISABLED_CODE = 5010;
    public static final String BATCH_CONTAINS_DISABLED_MSG = "批量操作中包含已禁用用户，已拒绝";
    public static final Integer ROLE_ASSIGN_DUPLICATE_CODE = 5011;
    public static final String ROLE_ASSIGN_DUPLICATE_MSG = "角色分配冲突：已存在相同或互斥的范围记录";
    public static final Integer ROLE_SCOPE_INVALID_CODE = 5012;
    public static final String ROLE_SCOPE_INVALID_MSG = "角色数据范围不合法";
    public static final Integer ROLE_REVOKE_SELF_CODE = 5013;
    public static final String ROLE_REVOKE_SELF_MSG = "不能撤销自己当前持有的角色";
    public static final Integer USER_ROLE_NOT_FOUND_CODE = 5014;
    public static final String USER_ROLE_NOT_FOUND_MSG = "用户角色关联不存在";
    //    ======================用户============================
//    用户自己输入密码或姓名异常

    public static final Integer RGEISTER_PASSWORD_OR_NAMEC_CODE = 1002;
    public static final String RGEISTER_PASSWORD_OR_NAMEC_MSG = "密码或姓名为空,请重新输入";
    //用户存在异常
    public static final Integer RGEISTER_STATUS_CODE = 1003;
    public static final String RGEISTER_STATUS_CODE_MSG = "用户不存在,请重新输入学号或工号";
    //    用户密码问题
    public static final Integer RGEISTER_PASSWORD_CODE = 1004;
    public static final String RGEISTER_PASSWORD_MSG = "密码不匹配,请重新输入";
    //    用户序号问题
    public static final Integer Author_ID_CODE = 1005;
    public static final String Author_ID_MSG = "用户序号为空,异常警告";


    //用户名已存在
    // 用户名已存在
    public static final Integer USERNAME_EXIST_CODE = 1006;
    public static final String USERNAME_EXIST_MSG = "用户名已存在";
    // 用户实体为空
    public static final Integer USER_EMPTY_CODE = 1007;
    public static final String USER_EMPTY_MSG = "用户实体为空";

    // 用户名为空
    public static final Integer USERNAME_BLANK_CODE = 1008;
    public static final String USERNAME_BLANK_MSG = "用户名为空";

    //非法字符
    public static final Integer ILLEGAL_Character_CODE = 2001;
    public static final String ILLEGAL_Character_MSG = "包含非法字符";


//无权删除用户

    public static final Integer NO_PERM_DELETE_USER_CODE = 0002;
    public static final String NO_PERM_DELETE_USER_MSG = "无权改变用户: ";



    /*


     * token验证
     *
     *
     *
     * */
//    验证失败
    public static final Integer TOKEN_FLASE_CODE = 3001;
    public static final String TOKEN_FLASE_MSG = "token解析异常";
    public static final int TOKEN_EXPIRED_CODE = 3002;
    public static final String TOKEN_EXPIRED_MSG = "token已过期";

    public static final int TOKEN_MISSING = 3003;
    public static final String TOKEN_MISSING_MSG = "请求中未携带token";
    // ========== 登录/Token ==========
    public static final Integer TOKEN_ERROR_CODE = 3004;
    public static final String TOKEN_ERROR_MSG = "token解析异常";

    public static final Integer NOT_LOGGED_IN_CODE = 3005;
    public static final String NOT_LOGGED_IN_MSG = "未登录";
    /*

    *
    *

     * 活动相关
     */
    //    ======================活动分类============================
//活动后缀为空
    public static final int ACT_CATEGORYID_NULL_CODE = 4001;
    public static final String ACT_CATEGORYID_NULL_MSG = "该活动后缀为空";
    //    对应活动内容为空
    public static final int ACT_CONTENT_NULL_CODE = 4002;
    public static final String ACT_CONTENT_NULL_MSG = "活动分类id为空或";
    public static final int ACT_NO_NULL_CODE = 4003;
    public static final String ACT_NO_NULL_MSG = "活动编号为空";





}
