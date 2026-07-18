package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.ActivityApply;
import untiy.entity.ActivityApproveFlow;
import untiy.entity.ActivitySign;
import untiy.entity.SysDepartment;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.ActivityApproveFlowMapper;
import untiy.mapper.ActivitySignMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.entity.SysClub;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社团解散执行：更新社团状态、删部门、清理活动、移除角色绑定。
 */
@Slf4j
@Component
public class ClubDissolveExecutor {

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysDepartmentMapper sysDepartmentMapper;

    @Autowired
    private ActivityApplyMapper activityApplyMapper;

    @Autowired
    private ActivityApproveFlowMapper activityApproveFlowMapper;

    @Autowired
    private ActivitySignMapper activitySignMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Transactional
    public void executeDissolve(Long clubId) {
        SysClub club = sysClubMapper.selectById(clubId);
        if (club == null) {
            return;
        }
        club.setStatus(ClubApplyConstants.CLUB_STATUS_DISSOLVED);
        club.setDissolveTime(LocalDateTime.now());
        sysClubMapper.updateById(club);

        List<SysDepartment> departments = sysDepartmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getClubId, clubId));
        Set<Long> deptIds = departments.stream().map(SysDepartment::getId).collect(Collectors.toSet());

        cleanActivities(clubId);
        removeClubRoleBindings(clubId);
        removeDepartmentRoleBindings(deptIds);
        removeMemberRoleBindings(deptIds);

        if (!departments.isEmpty()) {
            sysDepartmentMapper.delete(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getClubId, clubId));
        }
        log.info("社团 id={} 已执行解散", clubId);
    }

    private void cleanActivities(Long clubId) {
        LocalDateTime now = LocalDateTime.now();
        List<ActivityApply> activities = activityApplyMapper.selectList(
                new LambdaQueryWrapper<ActivityApply>().eq(ActivityApply::getClubId, clubId));
        for (ActivityApply activity : activities) {
            boolean keep = activity.getApproveStatus() != null && activity.getApproveStatus() == 4
                    && activity.getEndTime() != null && activity.getEndTime().isBefore(now);
            if (keep) {
                continue;
            }
            activityApproveFlowMapper.delete(new LambdaQueryWrapper<ActivityApproveFlow>()
                    .eq(ActivityApproveFlow::getActivityId, activity.getId()));
            activitySignMapper.delete(new LambdaQueryWrapper<ActivitySign>()
                    .eq(ActivitySign::getActivityId, activity.getId()));
            activityApplyMapper.deleteById(activity.getId());
        }
    }

    private void removeClubRoleBindings(Long clubId) {
        SysRole presidentRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, ClubApplyConstants.ROLE_CLUB_PRESIDENT)
                .last("LIMIT 1"));
        if (presidentRole == null) {
            return;
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, presidentRole.getId())
                .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_CLUB)
                .eq(SysUserRole::getScopeId, clubId));
    }

    private void removeDepartmentRoleBindings(Set<Long> deptIds) {
        if (deptIds.isEmpty()) {
            return;
        }
        SysRole ministerRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, ClubApplyConstants.ROLE_CLUB_MINISTER)
                .last("LIMIT 1"));
        if (ministerRole == null) {
            return;
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, ministerRole.getId())
                .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_DEPARTMENT)
                .in(SysUserRole::getScopeId, deptIds));
    }

    private void removeMemberRoleBindings(Set<Long> deptIds) {
        if (deptIds.isEmpty()) {
            return;
        }
        SysRole memberRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, ClubApplyConstants.ROLE_MEMBER)
                .last("LIMIT 1"));
        if (memberRole == null) {
            return;
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, memberRole.getId())
                .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_DEPARTMENT)
                .in(SysUserRole::getScopeId, deptIds));
    }

    /** 是否存在进行中活动（草稿/待审/审批中） */
    public boolean hasActiveActivities(Long clubId) {
        long count = activityApplyMapper.selectCount(new LambdaQueryWrapper<ActivityApply>()
                .eq(ActivityApply::getClubId, clubId)
                .in(ActivityApply::getApproveStatus, 1, 2, 3, 7));
        return count > 0;
    }

    public boolean isClubNameDuplicateInCollege(Long collegeId, String clubName) {
        if (collegeId == null || clubName == null) {
            return false;
        }
        long count = sysClubMapper.selectCount(new LambdaQueryWrapper<SysClub>()
                .eq(SysClub::getCollegeId, collegeId)
                .eq(SysClub::getClubName, clubName));
        return count > 0;
    }
}
