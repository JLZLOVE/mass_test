package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import untiy.entity.BaseQuery;
import untiy.entity.SysUser;

/**
 * 数据范围过滤：从 {@link LoginUserDetails} 读取等级，注入 QueryWrapper / BaseQuery 条件。
 */
public final class DataScopeHelper {

    private DataScopeHelper() {
    }

    public static LoginUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUserDetails)) {
            return null;
        }
        return (LoginUserDetails) auth.getPrincipal();
    }

    /**
     * 将五级可见范围应用到 BaseQuery（供 LevelAspect 或服务层复用）。
     */
    public static void applyToBaseQuery(BaseQuery query, LoginUserDetails user) {
        if (query == null || user == null) {
            return;
        }
        applyLevelScope(query, user.getEffectiveLevel(), user);
    }

    public static void applyLevelScope(BaseQuery query, int level, LoginUserDetails user) {
        switch (level) {
            case 0:
                break;
            case 1:
                query.setUserType(1);
                break;
            case 2:
                query.setClubId(user.getPrimaryClubId());
                break;
            case 3:
                query.setDepartmentId(user.getPrimaryDepartmentId());
                break;
            case 4:
            default:
                query.setUserId(user.getUserId());
                break;
        }
    }

    /**
     * 用户列表查询的数据范围（sys_user 无 club_id，通过 sys_user_role 子查询）。
     */
    public static void applySysUserScope(QueryWrapper<SysUser> wrapper) {
        LoginUserDetails user = currentUser();
        if (user == null) {
            return;
        }
        applySysUserScope(wrapper, user);
    }

    /**
     * Lambda 版数据范围过滤，避免硬编码列名。
     */
    public static void applySysUserScope(LambdaQueryWrapper<SysUser> wrapper) {
        LoginUserDetails user = currentUser();
        if (user == null) {
            return;
        }
        applySysUserScope(wrapper, user);
    }

    private static void applySysUserScope(QueryWrapper<SysUser> wrapper, LoginUserDetails user) {
        switch (user.getEffectiveLevel()) {
            case 0:
                break;
            case 1:
                wrapper.eq("user_type", 1);
                break;
            case 2:
                if (user.getPrimaryClubId() != null) {
                    wrapper.inSql("id",
                            "SELECT user_id FROM sys_user_role WHERE scope_type = 2 AND scope_id = "
                                    + user.getPrimaryClubId());
                }
                break;
            case 3:
                if (user.getPrimaryDepartmentId() != null) {
                    wrapper.inSql("id",
                            "SELECT user_id FROM sys_user_role WHERE scope_type = 3 AND scope_id = "
                                    + user.getPrimaryDepartmentId());
                }
                break;
            case 4:
            default:
                wrapper.eq("id", user.getUserId());
                break;
        }
    }

    private static void applySysUserScope(LambdaQueryWrapper<SysUser> wrapper, LoginUserDetails user) {
        switch (user.getEffectiveLevel()) {
            case 0:
                break;
            case 1:
                wrapper.eq(SysUser::getUserType, 1);
                break;
            case 2:
                if (user.getPrimaryClubId() != null) {
                    wrapper.inSql(SysUser::getId,
                            "SELECT user_id FROM sys_user_role WHERE scope_type = 2 AND scope_id = "
                                    + user.getPrimaryClubId());
                }
                break;
            case 3:
                if (user.getPrimaryDepartmentId() != null) {
                    wrapper.inSql(SysUser::getId,
                            "SELECT user_id FROM sys_user_role WHERE scope_type = 3 AND scope_id = "
                                    + user.getPrimaryDepartmentId());
                }
                break;
            case 4:
            default:
                wrapper.eq(SysUser::getId, user.getUserId());
                break;
        }
    }
}
