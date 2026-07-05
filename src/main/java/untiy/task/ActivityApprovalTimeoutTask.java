package untiy.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import untiy.entity.ActivityApply;
import untiy.entity.ActivityApproveFlow;
import untiy.entity.SysClub;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.ActivityApproveFlowMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ActivityApproverHelper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 审批超时：3天催办（日志），7天自动转交上级。
 */
@Slf4j
@Component
public class ActivityApprovalTimeoutTask {

    private static final int REMIND_DAYS = 3;
    private static final int ESCALATE_DAYS = 7;

    @Autowired
    private ActivityApplyMapper activityApplyMapper;

    @Autowired
    private ActivityApproveFlowMapper activityApproveFlowMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Scheduled(cron = "0 0 * * * ?")
    public void scanTimeoutSteps() {
        List<Integer> activeStatuses = Arrays.asList(
                ActivityApplyConstants.STATUS_PENDING,
                ActivityApplyConstants.STATUS_IN_PROGRESS,
                ActivityApplyConstants.STATUS_CHANGE_PENDING);
        List<ActivityApply> activities = activityApplyMapper.selectList(
                new LambdaQueryWrapper<ActivityApply>().in(ActivityApply::getApproveStatus, activeStatuses));
        LocalDateTime now = LocalDateTime.now();
        for (ActivityApply apply : activities) {
            processActivity(apply, now);
        }
    }

    private void processActivity(ActivityApply apply, LocalDateTime now) {
        int flowType = apply.getApproveStatus() == ActivityApplyConstants.STATUS_CHANGE_PENDING
                ? ActivityApplyConstants.FLOW_TYPE_CHANGE
                : ActivityApplyConstants.FLOW_TYPE_NORMAL;
        ActivityApproveFlow flow = activityApproveFlowMapper.selectOne(new LambdaQueryWrapper<ActivityApproveFlow>()
                .eq(ActivityApproveFlow::getActivityId, apply.getId())
                .eq(ActivityApproveFlow::getFlowType, flowType)
                .eq(ActivityApproveFlow::getStep, apply.getCurrentApproveStep())
                .isNull(ActivityApproveFlow::getApproveTime));
        if (flow == null || flow.getStepEnterTime() == null) {
            return;
        }
        LocalDateTime enterTime = flow.getStepEnterTime();
        if (enterTime.plusDays(REMIND_DAYS).isBefore(now) && enterTime.plusDays(ESCALATE_DAYS).isAfter(now)) {
            log.warn("[活动审批催办] 活动 {} 步骤 {} 已超过{}天未处理，当前审批人 userId={}",
                    apply.getActivityNo(), flow.getStep(), REMIND_DAYS, flow.getApproveUserId());
        }
        if (enterTime.plusDays(ESCALATE_DAYS).isBefore(now)) {
            escalate(apply, flow);
        }
    }

    private void escalate(ActivityApply apply, ActivityApproveFlow flow) {
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        if (club == null) {
            return;
        }
        String approverType = ActivityApproverHelper.inferApproverTypeFromUser(
                flow.getApproveUserId(), club, sysUserRoleMapper, sysRoleMapper, sysCollegeMapper);
        if (approverType == null) {
            return;
        }
        Long nextUserId = ActivityApproverHelper.resolveEscalationUserId(
                approverType, club, sysUserRoleMapper, sysRoleMapper, sysCollegeMapper);
        if (nextUserId == null || nextUserId.equals(flow.getApproveUserId())) {
            return;
        }
        flow.setApproveUserId(nextUserId);
        flow.setStepEnterTime(LocalDateTime.now());
        activityApproveFlowMapper.updateById(flow);
        log.info("[活动审批转交] 活动 {} 步骤 {} 已转交至 userId={}", apply.getActivityNo(), flow.getStep(), nextUserId);
    }
}
