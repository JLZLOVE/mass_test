package untiy.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import untiy.entity.SysClub;
import untiy.entity.SysDepartment;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.constants.NoticeConstants;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知接收人解析与发送范围校验。
 */
public final class NoticeScopeHelper {

    private NoticeScopeHelper() {
    }

    public static String resolvePublisherType(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                              SysClubMapper clubMapper, Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = loadActiveRoles(userRoleMapper, roleMapper, userId);
        int level = UserScopeResolver.resolveEffectiveLevel(roles);
        if (level == Level.SUPER_ADMIN) {
            return NoticeConstants.PUBLISHER_SUPER_ADMIN;
        }
        if (level == Level.ADMIN && roles.stream().anyMatch(r -> !isAdvisorCode(r.getRoleCode()))) {
            return NoticeConstants.PUBLISHER_ADMIN;
        }
        Long clubId = UserScopeResolver.resolvePrimaryClubId(userRoles, roles);
        if (clubId != null) {
            return NoticeConstants.PUBLISHER_PRESIDENT;
        }
        Long deptId = UserScopeResolver.resolvePrimaryDepartmentId(userRoles, roles);
        if (deptId != null) {
            return NoticeConstants.PUBLISHER_MINISTER;
        }
        List<SysClub> advised = clubMapper.selectList(
                new LambdaQueryWrapper<SysClub>().eq(SysClub::getAdvisorId, userId));
        if (!advised.isEmpty() || roles.stream().anyMatch(r -> isAdvisorCode(r.getRoleCode()))) {
            return NoticeConstants.PUBLISHER_ADVISOR;
        }
        if (level <= Level.ADMIN) {
            return NoticeConstants.PUBLISHER_ADMIN;
        }
        throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
    }

    public static void assertCanSendToScope(String publisherType, Long userId,
                                            SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                            SysClubMapper clubMapper, SysDepartmentMapper departmentMapper,
                                            Integer receiverType, String receiverValues) {
        if (publisherType == null) {
            throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
        }
        switch (publisherType) {
            case NoticeConstants.PUBLISHER_ADMIN:
            case NoticeConstants.PUBLISHER_SUPER_ADMIN:
                return;
            case NoticeConstants.PUBLISHER_PRESIDENT:
                assertPresidentScope(userRoleMapper, roleMapper, userId, receiverType, receiverValues);
                return;
            case NoticeConstants.PUBLISHER_MINISTER:
                assertMinisterScope(userRoleMapper, roleMapper, departmentMapper, userId, receiverType, receiverValues);
                return;
            case NoticeConstants.PUBLISHER_ADVISOR:
                assertAdvisorScope(clubMapper, userId, receiverType, receiverValues);
                return;
            default:
                throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
        }
    }

    public static Set<Long> resolveReceiverUserIds(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                                                   SysRoleMapper roleMapper, Integer receiverType,
                                                   String receiverValues) {
        Set<Long> userIds = new HashSet<>();
        if (receiverType == null) {
            return userIds;
        }
        switch (receiverType) {
            case NoticeConstants.RECEIVER_ALL_STUDENTS:
                userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUserType, 1).eq(SysUser::getStatus, 1))
                        .forEach(u -> userIds.add(u.getId()));
                break;
            case NoticeConstants.RECEIVER_ALL_TEACHERS:
                userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUserType, 2).eq(SysUser::getStatus, 1))
                        .forEach(u -> userIds.add(u.getId()));
                break;
            case NoticeConstants.RECEIVER_ROLES:
                List<Long> roleIds = parseLongList(receiverValues);
                if (!roleIds.isEmpty()) {
                    userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                                    .in(SysUserRole::getRoleId, roleIds))
                            .forEach(ur -> userIds.add(ur.getUserId()));
                }
                break;
            case NoticeConstants.RECEIVER_CLUBS:
                List<Long> clubIds = parseLongList(receiverValues);
                if (!clubIds.isEmpty()) {
                    userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                                    .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_CLUB)
                                    .in(SysUserRole::getScopeId, clubIds))
                            .forEach(ur -> userIds.add(ur.getUserId()));
                }
                break;
            case NoticeConstants.RECEIVER_USERS:
                userIds.addAll(parseLongList(receiverValues));
                break;
            default:
                break;
        }
        userIds.remove(null);
        return userIds;
    }

    public static boolean isReceiver(Long userId, Integer receiverType, String receiverValues,
                                     Integer longTermVisible, Long noticeId,
                                     SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                                     SysRoleMapper roleMapper,
                                     java.util.function.Function<Long, Boolean> fixedReceiverChecker) {
        if (longTermVisible != null && longTermVisible == 0) {
            return fixedReceiverChecker.apply(noticeId);
        }
        return resolveReceiverUserIds(userMapper, userRoleMapper, roleMapper, receiverType, receiverValues)
                .contains(userId);
    }

    public static boolean shouldHighlight(Integer importance, Integer urgency) {
        return (importance != null && importance == NoticeConstants.IMPORTANCE_HIGH)
                || (urgency != null && urgency == NoticeConstants.URGENCY_URGENT);
    }

    public static boolean shouldPin(Integer importance, Integer urgency, Boolean manualPinned) {
        if (Boolean.TRUE.equals(manualPinned)) {
            return true;
        }
        return (importance != null && importance <= NoticeConstants.IMPORTANCE_MEDIUM)
                || (urgency != null && urgency == NoticeConstants.URGENCY_URGENT);
    }

    private static void assertPresidentScope(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                            Long userId, Integer receiverType, String receiverValues) {
        Long clubId = resolvePresidentClubId(userRoleMapper, roleMapper, userId);
        if (clubId == null) {
            throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
        }
        if (receiverType == NoticeConstants.RECEIVER_CLUBS) {
            List<Long> targets = parseLongList(receiverValues);
            if (targets.size() == 1 && Objects.equals(targets.get(0), clubId)) {
                return;
            }
        }
        if (receiverType == NoticeConstants.RECEIVER_USERS) {
            return;
        }
        throw new EIException(ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_CODE,
                ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_MSG);
    }

    private static void assertMinisterScope(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                                            SysDepartmentMapper departmentMapper, Long userId,
                                            Integer receiverType, String receiverValues) {
        Long deptId = resolveMinisterDeptId(userRoleMapper, roleMapper, userId);
        if (deptId == null) {
            throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
        }
        if (receiverType == NoticeConstants.RECEIVER_USERS) {
            return;
        }
        if (receiverType == NoticeConstants.RECEIVER_ROLES) {
            return;
        }
        throw new EIException(ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_CODE,
                ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_MSG);
    }

    private static void assertAdvisorScope(SysClubMapper clubMapper, Long userId,
                                           Integer receiverType, String receiverValues) {
        List<Long> advisedClubIds = clubMapper.selectList(
                        new LambdaQueryWrapper<SysClub>().eq(SysClub::getAdvisorId, userId))
                .stream().map(SysClub::getId).collect(Collectors.toList());
        if (receiverType == NoticeConstants.RECEIVER_CLUBS) {
            List<Long> targets = parseLongList(receiverValues);
            if (!targets.isEmpty() && advisedClubIds.containsAll(targets)) {
                return;
            }
            throw new EIException(ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_CODE,
                    ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_MSG);
        }
        if (receiverType == NoticeConstants.RECEIVER_USERS) {
            return;
        }
        throw new EIException(ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_CODE,
                ErrorConfig.NOTICE_SCOPE_APPROVAL_REQUIRED_MSG);
    }

    private static Long resolvePresidentClubId(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = loadActiveRoles(userRoleMapper, roleMapper, userId);
        return UserScopeResolver.resolvePrimaryClubId(userRoles, roles);
    }

    private static Long resolveMinisterDeptId(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper, Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = loadActiveRoles(userRoleMapper, roleMapper, userId);
        return UserScopeResolver.resolvePrimaryDepartmentId(userRoles, roles);
    }

    private static List<Long> parseLongList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            JSONArray arr = JSON.parseArray(json);
            List<Long> list = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.getLong(i));
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

    private static boolean isAdvisorCode(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        String upper = roleCode.toUpperCase(Locale.ROOT);
        return upper.equals("ADVISOR") || upper.startsWith("ADVISOR");
    }
}
