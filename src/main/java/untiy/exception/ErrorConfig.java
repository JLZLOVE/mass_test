package untiy.exception;

public class ErrorConfig {

    //    注册时 ,后台添加不上
    public static final Integer RGEISTER_ADD_NEW_USER_CODE = 1001;
    public static final String RGEISTER_ADD_NEW_USER_MSG = "注册失败,请重新尝试";
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
