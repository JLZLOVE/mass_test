package untiy.exception;



/**
 * 用户权限校验错误码
 */
public class UserPermissionCode {



    // ========== 权限校验 ==========
    public static final Integer PERMISSION_DENIED_CODE = 1001;
    public static final String PERMISSION_DENIED_MSG = "权限不足";

    public static final Integer SELF_DELETE_CODE = 1002;
    public static final String SELF_DELETE_MSG = "不能删除自己";

    public static final Integer LEVEL_INSUFFICIENT_CODE = 1003;
    public static final String LEVEL_INSUFFICIENT_MSG = "当前角色等级不足以删除该用户";

    public static final Integer SCOPE_MISMATCH_CODE = 1004;
    public static final String SCOPE_MISMATCH_MSG = "目标用户不在管辖范围内";

    public static final Integer TARGET_NOT_FOUND_CODE = 1005;
    public static final String TARGET_NOT_FOUND_MSG = "目标用户不存在";

    public static final Integer TARGET_NO_ROLE_CODE = 1006;
    public static final String TARGET_NO_ROLE_MSG = "目标用户无角色信息";

    public static final Integer CURRENT_NO_SCOPE_CODE = 1007;
    public static final String CURRENT_NO_SCOPE_MSG = "当前用户无社团范围";



    private UserPermissionCode() {
        // 工具类私有构造
    }
}