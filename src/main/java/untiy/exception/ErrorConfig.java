package untiy.exception;

public class ErrorConfig
{
//    注册时 ,后台添加不上
    public static final Integer RGEISTER_ADD_NEW_USER_CODE=1001;
    public static final String RGEISTER_ADD_NEW_USER_MSG="注册失败,请重新尝试";
//    用户自己输入密码或姓名异常

    public static final Integer  RGEISTER_PASSWORD_OR_NAMEC_CODE=1002;
    public static final String  RGEISTER_PASSWORD_OR_NAMEC_MSG="密码或姓名为空,请重新输入";
//用户存在异常
    public static final Integer RGEISTER_STATUS_CODE=1003;
    public static final String  RGEISTER_STATUS_CODE_MSG="用户不存在,请重新输入学号或工号";
//    用户密码问题
    public static final Integer RGEISTER_PASSWORD_CODE=1004;
    public static final String RGEISTER_PASSWORD_MSG="密码不匹配,请重新输入";
//    用户序号问题
    public static final Integer Author_ID_CODE=1005;
    public static final String Author_ID_MSG="用户序号为空,异常警告";
//非法字符
    public static final Integer ILLEGAL_Character_CODE=2001;
    public static final String ILLEGAL_Character_MSG="包含非法字符";
    /*
    * token验证*/
//    验证失败
   public static final Integer TOKEN_FLASE_CODE=3001;
 public static final String TOKEN_FLASE_MSG="token解析异常";
    public static final int TOKEN_EXPIRED_CODE = 3002;
    public static final String TOKEN_EXPIRED_MSG = "token已过期";

    public static final int TOKEN_MISSING = 3003;
    public static final String TOKEN_MISSING_MSG = "请求中未携带token";

}
