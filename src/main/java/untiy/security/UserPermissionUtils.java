package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.UserPermissionCode;
import untiy.service.SysRoleService;
import untiy.service.SysUserRoleService;
import untiy.service.SysUserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户删除权限校验工具。
 * <p>
 * 规则（按优先级）：
 * <ol>
 *   <li>不能删自己</li>
 *   <li>仅社长及以上（role_level ≤ 2）可删除用户</li>
 *   <li>当前用户等级必须 < 目标用户等级（数字越小权限越高：1=管理员 > 2=社长 > 3=部长）</li>
 *   <li>数据范围校验：当前用户与目标用户必须在同一社团（scope_type=2）内</li>
 * </ol>
 */
@Component
public class UserPermissionUtils {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 自动从 SecurityContext 获取当前用户进行校验
     */
    public void checkDeletePermission(Long targetUserId) {
        LoginUserDetails currentUser = getCurrentUser();
        checkDeletePermission(currentUser, targetUserId);
    }

    /**
     * 校验当前用户是否有权删除目标用户，不满足任一规则则抛出 EIException
     */
    public void checkDeletePermission(LoginUserDetails currentUser, Long targetUserId) {
        // 规则0：登录校验
        if (currentUser == null) {
            throw new EIException(ErrorConfig.NOT_LOGGED_IN_CODE, ErrorConfig.NOT_LOGGED_IN_MSG);
        }
        if (targetUserId == null) {
            throw new EIException(4000, "目标用户ID不能为空");
        }

        // 规则1：不能删自己
        if (currentUser.getUserId().equals(targetUserId)) {
            throw new EIException(UserPermissionCode.SELF_DELETE_CODE, UserPermissionCode.SELF_DELETE_MSG);
        }

        // 规则2：仅社长及以上可删除（level ≤ 2）
        int currentLevel = currentUser.getEffectiveLevel();
        if (currentLevel > 2) {
            throw new EIException(UserPermissionCode.PERMISSION_DENIED_CODE,
                    "仅社长及以上有权删除用户，当前等级: " + currentLevel);
        }

        // 查询目标用户
        SysUser targetUser = sysUserService.getById(targetUserId);
        if (targetUser == null) {
            throw new EIException(UserPermissionCode.TARGET_NOT_FOUND_CODE, UserPermissionCode.TARGET_NOT_FOUND_MSG);
        }

        // 查询目标用户的角色
        List<SysUserRole> targetUserRoles = sysUserRoleService.list(
                new QueryWrapper<SysUserRole>().eq("user_id", targetUserId));
        if (targetUserRoles.isEmpty()) {
            throw new EIException(UserPermissionCode.TARGET_NO_ROLE_CODE, UserPermissionCode.TARGET_NO_ROLE_MSG);
        }

        // 规则3：当前等级必须 < 目标等级（数字越小等级越高）
        Set<Long> targetRoleIds = targetUserRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());
        List<SysRole> targetRoles = (List<SysRole>) sysRoleService.listByIds(targetRoleIds);
        int targetLevel = resolveEffectiveLevel(targetRoles);
        if (currentLevel >= targetLevel) {
            throw new EIException(UserPermissionCode.LEVEL_INSUFFICIENT_CODE,
                    String.format("当前等级(%d)不足，目标用户等级(%d)", currentLevel, targetLevel));
        }

        // 规则4：scope 校验 —— 必须在同一社团（scope_type = 2）
        Long currentClubId = currentUser.getPrimaryClubId();
        if (currentClubId == null) {
            throw new EIException(UserPermissionCode.CURRENT_NO_SCOPE_CODE, UserPermissionCode.CURRENT_NO_SCOPE_MSG);
        }
        boolean scopeMatched = targetUserRoles.stream()
                .anyMatch(ur -> ur.getScopeType() != null
                        && ur.getScopeType() == 2  // 2 = 社团
                        && currentClubId.equals(ur.getScopeId()));
        if (!scopeMatched) {
            throw new EIException(UserPermissionCode.SCOPE_MISMATCH_CODE, UserPermissionCode.SCOPE_MISMATCH_MSG);
        }
    }

    /**
     * 判断当前用户是否有权删除目标用户（不抛异常，返回布尔值）
     */
    public boolean canDelete(Long targetUserId) {
        try {
            checkDeletePermission(targetUserId);
            return true;
        } catch (EIException e) {
            return false;
        }
    }

    /**
     * 从角色列表解析最高等级（取最小值，数字越小权限越高）
     */
    private int resolveEffectiveLevel(List<SysRole> roles) {
        return roles.stream()
                .mapToInt(SysRole::getRoleLevel)
                .min()
                .orElse(99); // 无角色视为最低权限
    }

    /**
     * 从 SecurityContext 获取当前登录用户
     */
    private LoginUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUserDetails) {
            return (LoginUserDetails) principal;
        }
        return null;
    }
}