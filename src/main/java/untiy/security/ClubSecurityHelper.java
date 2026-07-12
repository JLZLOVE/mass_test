package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import untiy.entity.SysCollege;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.entity.SysUser;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 社团申请 / 合议相关的角色与学院范围校验。
 */
public final class ClubSecurityHelper {

    private ClubSecurityHelper() {
    }

    public static void assertHasAdvisorRole(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        if (!hasAdvisorRole(userRoleMapper, roleMapper, userId)) {
            throw new EIException(ErrorConfig.CLUB_REQUIRE_ADVISOR_ROLE_CODE, ErrorConfig.CLUB_REQUIRE_ADVISOR_ROLE_MSG);
        }
    }

    public static boolean hasAdvisorRole(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        return loadActiveRoles(userRoleMapper, roleMapper, userId).stream()
                .anyMatch(ClubSecurityHelper::isAdvisorRoleCode);
    }

    public static void assertSuperAdmin(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        if (UserScopeResolver.resolveEffectiveLevel(loadActiveRoles(userRoleMapper, roleMapper, userId)) != Level.SUPER_ADMIN) {
            throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, ErrorConfig.ROLE_NO_PERMISSION_MSG);
        }
    }

    /** 校级审批：超管或有效等级为 ADMIN(1) 且非仅指导老师 */
    public static void assertSchoolAdmin(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        List<SysRole> roles = loadActiveRoles(userRoleMapper, roleMapper, userId);
        int level = UserScopeResolver.resolveEffectiveLevel(roles);
        if (level == Level.SUPER_ADMIN) {
            return;
        }
        if (level == Level.ADMIN && roles.stream().anyMatch(r -> !isAdvisorRoleCode(r.getRoleCode()))) {
            return;
        }
        throw new EIException(ErrorConfig.CLUB_REQUIRE_ADMIN_ROLE_CODE, ErrorConfig.CLUB_REQUIRE_ADMIN_ROLE_MSG);
    }

    public static void assertDeanOfCollege(SysCollegeMapper collegeMapper, Long userId, Long collegeId) {
        SysCollege college = collegeMapper.selectById(collegeId);
        if (college == null || college.getDeanId() == null || !college.getDeanId().equals(userId)) {
            throw new EIException(ErrorConfig.CLUB_NOT_DEAN_CODE, ErrorConfig.CLUB_NOT_DEAN_MSG);
        }
    }

    public static void assertNotApplicant(Long currentUserId, Long applicantId) {
        if (Objects.equals(currentUserId, applicantId)) {
            throw new EIException(ErrorConfig.CLUB_CANNOT_APPROVE_SELF_CODE, ErrorConfig.CLUB_CANNOT_APPROVE_SELF_MSG);
        }
    }

    /**
     * 超管/管理员学院范围：data_scope=1 视为全校；否则须 scope_type=1 且 scope_id 匹配。
     */
    public static void assertCollegeInScope(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                            Long userId, Long collegeId) {
        if (collegeId == null) {
            throw new EIException(ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_CODE, ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_MSG);
        }
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = loadActiveRoles(userRoleMapper, roleMapper, userId);
        int level = UserScopeResolver.resolveEffectiveLevel(roles);
        if (level > Level.ADMIN) {
            throw new EIException(ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_CODE, ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_MSG);
        }

        for (SysRole role : roles) {
            if (!ClubApplyConstants.ROLE_SUPER_ADMIN.equals(role.getRoleCode()) && level != Level.ADMIN) {
                continue;
            }
            Integer dataScope = role.getDataScope();
            if (dataScope != null && dataScope == 1) {
                return;
            }
            if (dataScope != null && dataScope == 0) {
                return;
            }
            boolean scoped = userRoles.stream()
                    .filter(ur -> Objects.equals(ur.getRoleId(), role.getId()))
                    .anyMatch(ur -> Objects.equals(ur.getScopeType(), ClubApplyConstants.SCOPE_TYPE_COLLEGE)
                            && Objects.equals(ur.getScopeId(), collegeId));
            if (scoped) {
                return;
            }
        }
        throw new EIException(ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_CODE, ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_MSG);
    }

    public static void assertUserEnabled(SysUserMapper userMapper, Long userId) {
        SysUser user = userMapper.selectById(userId);
        UserSecurityHelper.assertUserEnabled(user);
    }

    public static void assertUserEnabledByUsername(SysUserMapper userMapper, String username) {
        if (username == null || username.isBlank()) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "用户名不能为空");
        }
        SysUser user = userMapper.selectByUsername(username);
        UserSecurityHelper.assertUserEnabled(user);
    }

    public static SysRole requireRoleByCode(SysRoleMapper roleMapper, String roleCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getStatus, 1)
                .last("LIMIT 1"));
        if (role == null) {
            throw new EIException(ErrorConfig.CLUB_ROLE_NOT_FOUND_CODE, ErrorConfig.CLUB_ROLE_NOT_FOUND_MSG);
        }
        return role;
    }

    private static List<SysRole> loadActiveRoles(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, 1));
    }

    private static boolean isAdvisorRoleCode(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        String upper = roleCode.toUpperCase(Locale.ROOT);
        return upper.equals("ADVISOR") || upper.startsWith("ADVISOR");
    }

    private static boolean isAdvisorRoleCode(SysRole role) {
        return isAdvisorRoleCode(role.getRoleCode());
    }
}
