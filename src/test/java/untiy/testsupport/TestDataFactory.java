package untiy.testsupport;

import untiy.entity.ActivityApply;
import untiy.entity.ActivitySignConfig;
import untiy.entity.NoticeInfo;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.ActivitySignConstants;
import untiy.entity.constants.NoticeConstants;
import untiy.exception.Level;

import java.time.LocalDateTime;

/**
 * 测试数据工厂，避免各测试类重复造数。
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static SysCollege college(Long id, String name) {
        SysCollege c = new SysCollege();
        c.setId(id);
        c.setCollegeName(name);
        c.setCollegeCode("C" + id);
        c.setCreateTime(LocalDateTime.now());
        return c;
    }

    public static SysClub club(Long id, String name, String category, Long collegeId) {
        SysClub club = new SysClub();
        club.setId(id);
        club.setClubName(name);
        club.setClubCode("CLUB" + id);
        club.setCategory(category);
        club.setCollegeId(collegeId);
        club.setStatus(1);
        club.setAdvisorId(100L);
        club.setCreateTime(LocalDateTime.now());
        return club;
    }

    public static ActivityApply activity(Long id, String no, Long clubId, int approveStatus) {
        LocalDateTime now = LocalDateTime.now();
        ActivityApply apply = new ActivityApply();
        apply.setId(id);
        apply.setActivityNo(no);
        apply.setClubId(clubId);
        apply.setActivityName("测试活动-" + no);
        apply.setStartTime(now.minusMinutes(10));
        apply.setEndTime(now.plusHours(2));
        apply.setLocation("测试场地");
        apply.setApplyUserId(1L);
        apply.setApproveStatus(approveStatus);
        apply.setActivityLevel(ActivityApplyConstants.LEVEL_COLLEGE);
        apply.setExpectedPeople(10);
        apply.setCreateTime(now.minusDays(1));
        return apply;
    }

    public static ActivityApply approvedActivity(Long id, String no, Long clubId) {
        return activity(id, no, clubId, ActivityApplyConstants.STATUS_APPROVED);
    }

    public static ActivitySignConfig signConfig(Long activityId, String activityNo,
                                               LocalDateTime start, LocalDateTime end,
                                               boolean enableCheckout) {
        ActivitySignConfig config = new ActivitySignConfig();
        config.setId(activityId);
        config.setActivityId(activityId);
        config.setActivityNo(activityNo);
        config.setEnabled(1);
        config.setSignMode(ActivitySignConstants.MODE_LOCATION);
        config.setSignStartTime(start);
        config.setSignEndTime(end);
        config.setSignRadius(ActivitySignConstants.DEFAULT_RADIUS_METERS);
        config.setEnableCheckout(enableCheckout ? 1 : 0);
        return config;
    }

    public static NoticeInfo notice(String no, String title, int receiverType, int status) {
        NoticeInfo n = new NoticeInfo();
        n.setNoticeNo(no);
        n.setTitle(title);
        n.setContent("content");
        n.setReceiverType(receiverType);
        n.setStatus(status);
        n.setPublisherId(1L);
        n.setPublishTime(LocalDateTime.now());
        n.setCreateTime(LocalDateTime.now());
        if (status == NoticeConstants.STATUS_PUBLISHED) {
            n.setPublishTime(LocalDateTime.now());
        }
        return n;
    }

    public static SysUser user(Long id, String username, int userType, int status) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setUsername(username);
        u.setRealName(username);
        u.setUserType(userType);
        u.setStatus(status);
        u.setPassword("$2a$10$dummy");
        return u;
    }

    public static SysRole role(Long id, String code, int roleLevel, Integer dataScope) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(code);
        role.setRoleLevel(roleLevel);
        role.setDataScope(dataScope);
        role.setStatus(1);
        return role;
    }

    public static SysUserRole userRole(Long id, Long userId, Long roleId, Integer scopeType, Long scopeId) {
        SysUserRole ur = new SysUserRole();
        ur.setId(id);
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setScopeType(scopeType);
        ur.setScopeId(scopeId);
        return ur;
    }

    public static SysUser student(Long id, String username) {
        return user(id, username, 1, 1);
    }

    public static int levelClubLeader() {
        return Level.CLUB_LEADER;
    }

    public static SysRole presidentRole(Long id) {
        return role(id, "CLUB_PRESIDENT", Level.CLUB_LEADER, 2);
    }

    public static SysRole superAdminRole(Long id) {
        SysRole r = role(id, "SUPER_ADMIN", Level.SUPER_ADMIN, 0);
        return r;
    }

    public static SysRole advisorRole(Long id) {
        return role(id, "ADVISOR", Level.ADMIN, 1);
    }
}
