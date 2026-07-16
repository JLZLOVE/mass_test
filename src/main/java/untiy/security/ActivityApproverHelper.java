package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysDepartment;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动审批：发起人识别、审批人查找。
 */
public final class ActivityApproverHelper {

    private ActivityApproverHelper() {
    }

    public static String resolveInitiatorType(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                              SysClubMapper clubMapper, SysCollegeMapper collegeMapper,
                                              SysDepartmentMapper departmentMapper,
                                              Long userId, Long clubId) {
        SysClub club = clubMapper.selectById(clubId);
        if (club == null || club.getStatus() == null || club.getStatus() != ClubApplyConstants.CLUB_STATUS_NORMAL) {
            throw new EIException(ErrorConfig.ACT_CLUB_NOT_FOUND_CODE, ErrorConfig.ACT_CLUB_NOT_FOUND_MSG);
        }
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = UserScopeResolver.loadActiveRoles(userRoleMapper, roleMapper, userId);

        if (club.getCollegeId() != null) {
            SysCollege college = collegeMapper.selectById(club.getCollegeId());
            if (college != null && Objects.equals(college.getDeanId(), userId)) {
                return ActivityApplyConstants.INITIATOR_DEAN_OR_ADMIN;
            }
        }
        int level = UserScopeResolver.resolveEffectiveLevel(roles);
        if (level == Level.SUPER_ADMIN) {
            return ActivityApplyConstants.INITIATOR_DEAN_OR_ADMIN;
        }
        if (level == Level.ADMIN && roles.stream().anyMatch(r -> !UserScopeResolver.isAdvisorRoleCode(r.getRoleCode()))) {
            return ActivityApplyConstants.INITIATOR_DEAN_OR_ADMIN;
        }
        if (Objects.equals(club.getAdvisorId(), userId)) {
            return ActivityApplyConstants.INITIATOR_ADVISOR;
        }
        if (isClubPresident(userRoles, roles, clubId)) {
            return ActivityApplyConstants.INITIATOR_PRESIDENT;
        }
        if (isClubMinister(userRoleMapper, roleMapper, departmentMapper, userId, clubId)) {
            return ActivityApplyConstants.INITIATOR_MINISTER;
        }
        throw new EIException(ErrorConfig.ACT_SUBMIT_NO_PERMISSION_CODE, ErrorConfig.ACT_SUBMIT_NO_PERMISSION_MSG);
    }

    public static Long resolveApproverUserId(String approverType, SysClub club,
                                             SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                             SysCollegeMapper collegeMapper) {
        switch (approverType) {
            case ActivityApplyConstants.APPROVER_PRESIDENT:
                return findPresidentUserId(userRoleMapper, roleMapper, club.getId());
            case ActivityApplyConstants.APPROVER_ADVISOR:
                return requireUserId(club.getAdvisorId(), "指导老师");
            case ActivityApplyConstants.APPROVER_COLLEGE_DEAN:
                return findCollegeDeanUserId(collegeMapper, club.getCollegeId());
            case ActivityApplyConstants.APPROVER_SUPER_ADMIN:
                return findSuperAdminUserId(userRoleMapper, roleMapper);
            default:
                throw new EIException(ErrorConfig.ACT_APPROVER_NOT_FOUND_CODE, ErrorConfig.ACT_APPROVER_NOT_FOUND_MSG);
        }
    }

    public static Long resolveRoleIdForApprover(String approverType, SysRoleMapper roleMapper) {
        switch (approverType) {
            case ActivityApplyConstants.APPROVER_PRESIDENT:
                return ClubSecurityHelper.requireRoleByCode(roleMapper, ClubApplyConstants.ROLE_CLUB_PRESIDENT).getId();
            case ActivityApplyConstants.APPROVER_ADVISOR:
                return findAdvisorRoleId(roleMapper);
            case ActivityApplyConstants.APPROVER_SUPER_ADMIN:
                return ClubSecurityHelper.requireRoleByCode(roleMapper, ClubApplyConstants.ROLE_SUPER_ADMIN).getId();
            case ActivityApplyConstants.APPROVER_COLLEGE_DEAN:
                return null;
            default:
                return null;
        }
    }

    /** 超时转交：社长→指导老师→学院书记→校书记 */
    public static Long resolveEscalationUserId(String approverType, SysClub club,
                                               SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                               SysCollegeMapper collegeMapper) {
        switch (approverType) {
            case ActivityApplyConstants.APPROVER_PRESIDENT:
                return club.getAdvisorId();
            case ActivityApplyConstants.APPROVER_ADVISOR:
                return findCollegeDeanUserId(collegeMapper, club.getCollegeId());
            case ActivityApplyConstants.APPROVER_COLLEGE_DEAN:
                return findSuperAdminUserId(userRoleMapper, roleMapper);
            default:
                return null;
        }
    }

    public static String inferApproverTypeFromUser(Long userId, SysClub club,
                                                   SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                                   SysCollegeMapper collegeMapper) {
        if (userId == null) {
            return null;
        }
        if (isClubPresident(userRoleMapper, roleMapper, userId, club.getId())) {
            return ActivityApplyConstants.APPROVER_PRESIDENT;
        }
        if (Objects.equals(club.getAdvisorId(), userId)) {
            return ActivityApplyConstants.APPROVER_ADVISOR;
        }
        if (club.getCollegeId() != null) {
            SysCollege college = collegeMapper.selectById(club.getCollegeId());
            if (college != null && Objects.equals(college.getDeanId(), userId)) {
                return ActivityApplyConstants.APPROVER_COLLEGE_DEAN;
            }
        }
        if (hasSuperAdminRole(userRoleMapper, roleMapper, userId)) {
            return ActivityApplyConstants.APPROVER_SUPER_ADMIN;
        }
        return null;
    }

    private static Long findPresidentUserId(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long clubId) {
        SysRole presidentRole = ClubSecurityHelper.requireRoleByCode(roleMapper, ClubApplyConstants.ROLE_CLUB_PRESIDENT);
        SysUserRole binding = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, presidentRole.getId())
                .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_CLUB)
                .eq(SysUserRole::getScopeId, clubId)
                .last("LIMIT 1"));
        if (binding == null || binding.getUserId() == null) {
            throw new EIException(ErrorConfig.ACT_APPROVER_PRESIDENT_NOT_FOUND_CODE, ErrorConfig.ACT_APPROVER_PRESIDENT_NOT_FOUND_MSG);
        }
        return binding.getUserId();
    }

    private static Long findCollegeDeanUserId(SysCollegeMapper collegeMapper, Long collegeId) {
        if (collegeId == null) {
            throw new EIException(ErrorConfig.ACT_APPROVER_NO_COLLEGE_CODE, ErrorConfig.ACT_APPROVER_NO_COLLEGE_MSG);
        }
        SysCollege college = collegeMapper.selectById(collegeId);
        return requireUserId(college != null ? college.getDeanId() : null, "学院书记");
    }

    private static Long findSuperAdminUserId(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper) {
        SysRole superRole = ClubSecurityHelper.requireRoleByCode(roleMapper, ClubApplyConstants.ROLE_SUPER_ADMIN);
        SysUserRole binding = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, superRole.getId())
                .last("LIMIT 1"));
        if (binding == null || binding.getUserId() == null) {
            throw new EIException(ErrorConfig.ACT_APPROVER_SUPER_ADMIN_NOT_FOUND_CODE, ErrorConfig.ACT_APPROVER_SUPER_ADMIN_NOT_FOUND_MSG);
        }
        return binding.getUserId();
    }

    private static Long findAdvisorRoleId(SysRoleMapper roleMapper) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .likeRight(SysRole::getRoleCode, "ADVISOR")
                .eq(SysRole::getStatus, 1)
                .last("LIMIT 1"));
        return role != null ? role.getId() : null;
    }

    private static Long requireUserId(Long userId, String label) {
        if (userId == null) {
            throw new EIException(ErrorConfig.ACT_APPROVER_NOT_FOUND_TEMPLATE_CODE,
                    String.format(ErrorConfig.ACT_APPROVER_NOT_FOUND_TEMPLATE_MSG, label));
        }
        return userId;
    }

    private static boolean isClubPresident(List<SysUserRole> userRoles, List<SysRole> roles, Long clubId) {
        Set<Long> presidentRoleIds = roles.stream()
                .filter(r -> ClubApplyConstants.ROLE_CLUB_PRESIDENT.equals(r.getRoleCode()))
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        return userRoles.stream()
                .anyMatch(ur -> presidentRoleIds.contains(ur.getRoleId())
                        && Objects.equals(ur.getScopeType(), ClubApplyConstants.SCOPE_TYPE_CLUB)
                        && Objects.equals(ur.getScopeId(), clubId));
    }

    private static boolean isClubPresident(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                           Long userId, Long clubId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = UserScopeResolver.loadActiveRoles(userRoleMapper, roleMapper, userId);
        return isClubPresident(userRoles, roles, clubId);
    }

    private static boolean isClubMinister(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                          SysDepartmentMapper departmentMapper, Long userId, Long clubId) {
        SysRole ministerRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, ClubApplyConstants.ROLE_CLUB_MINISTER)
                .eq(SysRole::getStatus, 1)
                .last("LIMIT 1"));
        if (ministerRole == null) {
            return false;
        }
        List<Long> deptIds = departmentMapper.selectList(
                        new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getClubId, clubId))
                .stream().map(SysDepartment::getId).collect(Collectors.toList());
        if (deptIds.isEmpty()) {
            return false;
        }
        long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, ministerRole.getId())
                .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_DEPARTMENT)
                .in(SysUserRole::getScopeId, deptIds));
        return count > 0;
    }

    private static boolean hasSuperAdminRole(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        List<SysRole> roles = UserScopeResolver.loadActiveRoles(userRoleMapper, roleMapper, userId);
        return roles.stream().anyMatch(r -> ClubApplyConstants.ROLE_SUPER_ADMIN.equals(r.getRoleCode()));
    }
}
