package untiy.security;

import untiy.entity.SysRole;
import untiy.entity.SysUserRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 根据用户角色与 sys_user_role 范围，解析有效等级与社团/部门 ID。
 */
public final class UserScopeResolver {

    /** role_code → 有效等级（数字越小权限越高） */
    private static final Map<String, Integer> ROLE_EFFECTIVE_LEVEL = new HashMap<>();

    static {
        ROLE_EFFECTIVE_LEVEL.put("SUPER_ADMIN", 0);
        ROLE_EFFECTIVE_LEVEL.put("ADVISOR", 1);
        ROLE_EFFECTIVE_LEVEL.put("CLUB_PRESIDENT", 2);
        ROLE_EFFECTIVE_LEVEL.put("CLUB_MINISTER", 3);
        ROLE_EFFECTIVE_LEVEL.put("MEMBER", 4);
    }

    private UserScopeResolver() {
    }

    /**
     * 取用户所有角色中最高的有效等级（数值最小）。
     */
    public static int resolveEffectiveLevel(List<SysRole> roles) {
        int level = 4;
        for (SysRole role : roles) {
            Integer mapped = ROLE_EFFECTIVE_LEVEL.get(role.getRoleCode());
            if (mapped != null) {
                level = Math.min(level, mapped);
            }
        }
        return level;
    }

    /**
     * 从 sys_user_role 中取社长角色的社团 scope_id（scope_type=2）。
     */
    public static Long resolvePrimaryClubId(List<SysUserRole> userRoles, List<SysRole> roles) {
        Set<Long> presidentRoleIds = roles.stream()
                .filter(r -> "CLUB_PRESIDENT".equals(r.getRoleCode()))
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        return userRoles.stream()
                .filter(ur -> presidentRoleIds.contains(ur.getRoleId()))
                .filter(ur -> ur.getScopeType() != null && ur.getScopeType() == 2)
                .map(SysUserRole::getScopeId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 从 sys_user_role 中取部长角色的部门 scope_id（scope_type=3）。
     */
    public static Long resolvePrimaryDepartmentId(List<SysUserRole> userRoles, List<SysRole> roles) {
        Set<Long> ministerRoleIds = roles.stream()
                .filter(r -> "CLUB_MINISTER".equals(r.getRoleCode()))
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        return userRoles.stream()
                .filter(ur -> ministerRoleIds.contains(ur.getRoleId()))
                .filter(ur -> ur.getScopeType() != null && ur.getScopeType() == 3)
                .map(SysUserRole::getScopeId)
                .findFirst()
                .orElse(null);
    }
}
