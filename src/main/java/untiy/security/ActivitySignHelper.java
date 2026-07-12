package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import untiy.entity.ActivityApply;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ActivitySignHelper {

    private ActivitySignHelper() {
    }

    public static void assertActivityOwnerOrAdvisor(ActivityApply apply, SysClub club,
                                                    Long userId, int effectiveLevel) {
        if (Objects.equals(apply.getApplyUserId(), userId)) {
            return;
        }
        if (club != null && Objects.equals(club.getAdvisorId(), userId)) {
            return;
        }
        if (effectiveLevel <= Level.ADMIN) {
            return;
        }
        throw new EIException(ErrorConfig.SIGN_NO_PERMISSION_CODE, ErrorConfig.SIGN_NO_PERMISSION_MSG);
    }

    public static void assertClubPresident(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                           Long userId, Long clubId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = loadActiveRoles(userRoleMapper, roleMapper, userId);
        Long presidentClub = UserScopeResolver.resolvePrimaryClubId(userRoles, roles);
        if (!Objects.equals(presidentClub, clubId)) {
            throw new EIException(ErrorConfig.SIGN_NO_PERMISSION_CODE, "仅社长可为成员发起补签");
        }
    }

    public static Long resolveMakeupApprover(int step, ActivityApply apply, SysClub club,
                                           SysCollegeMapper collegeMapper) {
        if (step == 1) {
            return club.getAdvisorId();
        }
        if (apply.getActivityLevel() != null
                && apply.getActivityLevel() == ActivityApplyConstants.LEVEL_SCHOOL
                && club.getCollegeId() != null) {
            SysCollege college = collegeMapper.selectById(club.getCollegeId());
            return college != null ? college.getDeanId() : null;
        }
        return null;
    }

    public static int makeupTotalSteps(ActivityApply apply) {
        if (apply.getActivityLevel() != null
                && apply.getActivityLevel() == ActivityApplyConstants.LEVEL_SCHOOL) {
            return 2;
        }
        return 1;
    }

    public static double distanceMeters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return Double.MAX_VALUE;
        }
        double r = 6371000;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static List<SysRole> loadActiveRoles(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds).eq(SysRole::getStatus, 1));
    }
}
