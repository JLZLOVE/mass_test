package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.ActivityApply;
import untiy.entity.ActivityApplyHistory;
import untiy.entity.ActivityApproveFlow;
import untiy.entity.ActivityCategory;
import untiy.entity.ActivitySignConfig;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysUser;
import untiy.security.DataScopeHelper;
import untiy.security.LoginUserDetails;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.ActivityApproveDTO;
import untiy.entity.dto.ActivityCancelDTO;
import untiy.entity.dto.ActivityChangeDTO;
import untiy.entity.dto.ActivitySubmitDTO;
import untiy.entity.dto.ActivitySummaryDTO;
import untiy.entity.vo.ActivityApplyDetailVO;
import untiy.entity.vo.PortalActivityDetailVO;
import untiy.entity.vo.PortalActivityListVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.ActivityApplyHistoryMapper;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.ActivityApproveFlowMapper;
import untiy.mapper.ActivityCategoryMapper;
import untiy.mapper.ActivitySignConfigMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ActivityApprovalChainHelper;
import untiy.security.ActivityApproverHelper;
import untiy.security.LoginUserDetails;
import untiy.security.UserExposeHelper;
import untiy.service.ActivityApplyService;
import untiy.service.NoticeAutoPublisher;
import untiy.utils.ActivityCodeGeneratorUtil;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityApplyServiceImpl extends ServiceImpl<ActivityApplyMapper, ActivityApply>
        implements ActivityApplyService {

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private SysDepartmentMapper sysDepartmentMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ActivityApproveFlowMapper activityApproveFlowMapper;

    @Autowired
    private ActivityApplyHistoryMapper activityApplyHistoryMapper;

    @Autowired
    private ActivityCodeGeneratorUtil activityCodeGeneratorUtil;

    @Autowired
    private NoticeAutoPublisher noticeAutoPublisher;

    @Autowired
    private ActivityCategoryMapper activityCategoryMapper;

    @Autowired
    private ActivitySignConfigMapper activitySignConfigMapper;

    @Transactional
    @Override
    public void submit(ActivitySubmitDTO dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
        validateActivityLevel(dto.getActivityLevel());

        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = requireNormalClub(dto.getClubId());
        String initiatorType = ActivityApproverHelper.resolveInitiatorType(
                sysUserRoleMapper, sysRoleMapper, sysClubMapper, sysCollegeMapper,
                sysDepartmentMapper, user.getUserId(), dto.getClubId());

        LocalDateTime now = LocalDateTime.now();
        ActivityApply apply = new ActivityApply();
        apply.setActivityNo(activityCodeGeneratorUtil.generateCode(dto.getClubId()));
        apply.setClubId(dto.getClubId());
        apply.setActivityName(dto.getActivityName());
        apply.setCategoryId(dto.getCategoryId());
        apply.setActivityType(dto.getActivityType());
        apply.setActivityLevel(dto.getActivityLevel());
        apply.setStartTime(dto.getStartTime());
        apply.setEndTime(dto.getEndTime());
        apply.setLocation(dto.getLocation());
        apply.setLocationDetail(dto.getLocationDetail());
        apply.setExpectedPeople(dto.getExpectedPeople());
        apply.setBudget(dto.getBudget());
        apply.setActivityContent(dto.getActivityContent());
        apply.setSafetyPlan(dto.getSafetyPlan());
        apply.setAttachment(dto.getAttachment());
        apply.setApplyUserId(user.getUserId());
        apply.setApplyTime(now);
        apply.setCurrentApproveStep(1);
        apply.setApproveStatus(ActivityApplyConstants.STATUS_PENDING);
        apply.setLevelAdjustLocked(0);
        apply.setVersion(0);
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        save(apply);

        createFlowSteps(apply, club, ActivityApprovalChainHelper.buildNormalChain(initiatorType, dto.getActivityLevel()),
                ActivityApplyConstants.FLOW_TYPE_NORMAL, now);
        log.info("用户 {} 提交活动申请 {}，发起人角色={}", user.getUsername(), apply.getActivityNo(), initiatorType);
    }

    @Transactional
    @Override
    public void approve(Long id, ActivityApproveDTO dto) {
        if (StringUtils.isBlank(dto.getOpinion())) {
            throw new EIException(ErrorConfig.ACT_OPINION_REQUIRED_CODE, ErrorConfig.ACT_OPINION_REQUIRED_MSG);
        }
        ActivityApply apply = requireApply(id);
        assertVersion(apply, dto.getVersion());
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = requireNormalClub(apply.getClubId());

        int flowType = apply.getApproveStatus() == ActivityApplyConstants.STATUS_CHANGE_PENDING
                ? ActivityApplyConstants.FLOW_TYPE_CHANGE
                : ActivityApplyConstants.FLOW_TYPE_NORMAL;
        ActivityApproveFlow currentFlow = requireCurrentFlow(apply, flowType);
        if (!Objects.equals(currentFlow.getApproveUserId(), user.getUserId())) {
            throw new EIException(ErrorConfig.ACT_NOT_CURRENT_APPROVER_CODE, ErrorConfig.ACT_NOT_CURRENT_APPROVER_MSG);
        }

        maybeAdjustLevel(apply, club, user.getUserId(), dto.getActivityLevel());

        LocalDateTime now = LocalDateTime.now();
        currentFlow.setApproveResult(ActivityApplyConstants.FLOW_RESULT_PASS);
        currentFlow.setApproveOpinion(dto.getOpinion());
        currentFlow.setApproveTime(now);
        activityApproveFlowMapper.updateById(currentFlow);

        int totalSteps = activityApproveFlowMapper.selectCount(new LambdaQueryWrapper<ActivityApproveFlow>()
                .eq(ActivityApproveFlow::getActivityId, apply.getId())
                .eq(ActivityApproveFlow::getFlowType, flowType)).intValue();
        int nextStep = apply.getCurrentApproveStep() + 1;

        if (nextStep > totalSteps) {
            finishApproval(apply, flowType, now);
        } else {
            apply.setCurrentApproveStep(nextStep);
            apply.setApproveStatus(flowType == ActivityApplyConstants.FLOW_TYPE_CHANGE
                    ? ActivityApplyConstants.STATUS_CHANGE_PENDING
                    : ActivityApplyConstants.STATUS_IN_PROGRESS);
            apply.setUpdateTime(now);
            if (!updateById(apply)) {
                throwVersionConflict();
            }
            ActivityApproveFlow nextFlow = activityApproveFlowMapper.selectOne(new LambdaQueryWrapper<ActivityApproveFlow>()
                    .eq(ActivityApproveFlow::getActivityId, apply.getId())
                    .eq(ActivityApproveFlow::getFlowType, flowType)
                    .eq(ActivityApproveFlow::getStep, nextStep));
            if (nextFlow != null) {
                nextFlow.setStepEnterTime(now);
                activityApproveFlowMapper.updateById(nextFlow);
            }
        }
        log.info("活动 {} 步骤 {} 审批通过", apply.getActivityNo(), currentFlow.getStep());
    }

    @Transactional
    @Override
    public void reject(Long id, ActivityApproveDTO dto) {
        if (StringUtils.isBlank(dto.getOpinion())) {
            throw new EIException(ErrorConfig.ACT_OPINION_REQUIRED_CODE, ErrorConfig.ACT_OPINION_REQUIRED_MSG);
        }
        ActivityApply apply = requireApply(id);
        assertVersion(apply, dto.getVersion());
        LoginUserDetails user = SecurityUtils.getCurrentUser();

        int flowType = apply.getApproveStatus() == ActivityApplyConstants.STATUS_CHANGE_PENDING
                ? ActivityApplyConstants.FLOW_TYPE_CHANGE
                : ActivityApplyConstants.FLOW_TYPE_NORMAL;
        ActivityApproveFlow currentFlow = requireCurrentFlow(apply, flowType);
        if (!Objects.equals(currentFlow.getApproveUserId(), user.getUserId())) {
            throw new EIException(ErrorConfig.ACT_NOT_CURRENT_APPROVER_CODE, ErrorConfig.ACT_NOT_CURRENT_APPROVER_MSG);
        }

        LocalDateTime now = LocalDateTime.now();
        currentFlow.setApproveResult(ActivityApplyConstants.FLOW_RESULT_REJECT);
        currentFlow.setApproveOpinion(dto.getOpinion());
        currentFlow.setApproveTime(now);
        activityApproveFlowMapper.updateById(currentFlow);

        if (flowType == ActivityApplyConstants.FLOW_TYPE_CHANGE) {
            markLatestHistoryRejected(apply.getId());
            apply.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        } else {
            apply.setApproveStatus(ActivityApplyConstants.STATUS_REJECTED);
        }
        apply.setRejectReason(dto.getOpinion());
        apply.setUpdateTime(now);
        if (!updateById(apply)) {
            throwVersionConflict();
        }
        log.info("活动 {} 步骤 {} 审批驳回", apply.getActivityNo(), currentFlow.getStep());
    }

    @Transactional
    @Override
    public void requestChange(Long id, ActivityChangeDTO dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
        ActivityApply apply = requireApply(id);
        assertVersion(apply, dto.getVersion());
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (!Objects.equals(apply.getApplyUserId(), user.getUserId())) {
            throw new EIException(ErrorConfig.ACT_NOT_APPLICANT_CODE, ErrorConfig.ACT_NOT_APPLICANT_MSG);
        }
        if (apply.getApproveStatus() == null || apply.getApproveStatus() != ActivityApplyConstants.STATUS_APPROVED) {
            throw new EIException(ErrorConfig.ACT_STATUS_INVALID_CODE, ErrorConfig.ACT_STATUS_INVALID_MSG);
        }

        LocalDateTime now = LocalDateTime.now();
        saveChangeHistory(apply, dto, ActivityApplyConstants.HISTORY_PENDING, now);

        SysClub club = requireNormalClub(apply.getClubId());
        activityApproveFlowMapper.delete(new LambdaQueryWrapper<ActivityApproveFlow>()
                .eq(ActivityApproveFlow::getActivityId, apply.getId())
                .eq(ActivityApproveFlow::getFlowType, ActivityApplyConstants.FLOW_TYPE_CHANGE));

        apply.setCurrentApproveStep(1);
        apply.setApproveStatus(ActivityApplyConstants.STATUS_CHANGE_PENDING);
        apply.setUpdateTime(now);
        if (!updateById(apply)) {
            throwVersionConflict();
        }

        createFlowSteps(apply, club, ActivityApprovalChainHelper.buildChangeChain(),
                ActivityApplyConstants.FLOW_TYPE_CHANGE, now);
        log.info("活动 {} 发起变更申请", apply.getActivityNo());
    }

    @Transactional
    @Override
    public void cancel(Long id, ActivityCancelDTO dto) {
        ActivityApply apply = requireApply(id);
        assertVersion(apply, dto.getVersion());
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (!Objects.equals(apply.getApplyUserId(), user.getUserId())) {
            throw new EIException(ErrorConfig.ACT_NOT_APPLICANT_CODE, ErrorConfig.ACT_NOT_APPLICANT_MSG);
        }
        Integer status = apply.getApproveStatus();
        if (status == null || status == ActivityApplyConstants.STATUS_CANCELLED
                || status == ActivityApplyConstants.STATUS_REJECTED) {
            throw new EIException(ErrorConfig.ACT_STATUS_INVALID_CODE, ErrorConfig.ACT_STATUS_INVALID_MSG);
        }

        apply.setApproveStatus(ActivityApplyConstants.STATUS_CANCELLED);
        apply.setRejectReason(dto.getReason());
        apply.setUpdateTime(LocalDateTime.now());
        if (!updateById(apply)) {
            throwVersionConflict();
        }
        noticeAutoPublisher.publishActivityCancelNotice(apply);
        log.info("活动 {} 已取消，原因：{}", apply.getActivityNo(), dto.getReason());
    }

    @Transactional
    @Override
    public void uploadSummary(Long id, ActivitySummaryDTO dto) {
        ActivityApply apply = requireApply(id);
        assertVersion(apply, dto.getVersion());
        if (apply.getApproveStatus() == null || apply.getApproveStatus() != ActivityApplyConstants.STATUS_APPROVED) {
            throw new EIException(ErrorConfig.ACT_STATUS_INVALID_CODE, "仅已通过的活动可上传总结");
        }
        if (apply.getEndTime() == null) {
            throw new EIException(ErrorConfig.ACT_SUMMARY_WINDOW_CODE, ErrorConfig.ACT_SUMMARY_WINDOW_MSG);
        }
        LocalDateTime now = LocalDateTime.now();
        long daysAfterEnd = ChronoUnit.DAYS.between(apply.getEndTime().toLocalDate(), now.toLocalDate());
        if (daysAfterEnd < 1 || daysAfterEnd > 3) {
            throw new EIException(ErrorConfig.ACT_SUMMARY_WINDOW_CODE, ErrorConfig.ACT_SUMMARY_WINDOW_MSG);
        }
        if (StringUtils.isBlank(dto.getSummaryContent()) && StringUtils.isBlank(dto.getSummaryAttachment())) {
            throw new EIException(ErrorConfig.ACT_CONTENT_NULL_CODE, "总结内容或附件至少填写一项");
        }

        apply.setSummaryContent(dto.getSummaryContent());
        apply.setSummaryAttachment(dto.getSummaryAttachment());
        apply.setSummaryUploadTime(now);
        apply.setUpdateTime(now);
        if (!updateById(apply)) {
            throwVersionConflict();
        }
    }

    @Override
    public ActivityApplyDetailVO getDetail(Long id) {
        ActivityApply apply = requireApply(id);
        ActivityApplyDetailVO vo = new ActivityApplyDetailVO();
        vo.setApply(apply);
        vo.setFlows(activityApproveFlowMapper.selectList(new LambdaQueryWrapper<ActivityApproveFlow>()
                .eq(ActivityApproveFlow::getActivityId, id)
                .orderByAsc(ActivityApproveFlow::getFlowType, ActivityApproveFlow::getStep)));
        vo.setHistories(activityApplyHistoryMapper.selectList(new LambdaQueryWrapper<ActivityApplyHistory>()
                .eq(ActivityApplyHistory::getActivityApplyId, id)
                .orderByDesc(ActivityApplyHistory::getCreateTime)));
        UserExposeHelper.enrichActivityApply(sysUserMapper, apply);
        UserExposeHelper.enrichApproveFlows(sysUserMapper, vo.getFlows());
        vo.setCurrentApproverName(resolveCurrentApproverName(apply));
        vo.setQueryTime(LocalDateTime.now());
        return vo;
    }

    @Override
    public IPage<ActivityApply> pageQuery(Map<String, Object> param, ActivityApply query) {
        Page<ActivityApply> page = MPUtil.getPage(param);
        QueryWrapper<ActivityApply> wrapper = MPUtil.sort(
                MPUtil.between(MPUtil.likeOrEq(new QueryWrapper<>(), query), param), param);

        // 数据范围过滤：Level 4 普通学生仅查看本人参与的活动
        LoginUserDetails currentUser = DataScopeHelper.currentUser();
        if (currentUser != null && currentUser.getEffectiveLevel() == 4) {
            wrapper.eq("apply_user_id", currentUser.getUserId());
        }

        IPage<ActivityApply> result = page(page, wrapper);
        UserExposeHelper.enrichActivityApplyList(sysUserMapper, result.getRecords());
        return result;
    }

    private void finishApproval(ActivityApply apply, int flowType, LocalDateTime now) {
        if (flowType == ActivityApplyConstants.FLOW_TYPE_CHANGE) {
            ActivityApplyHistory history = activityApplyHistoryMapper.selectOne(new LambdaQueryWrapper<ActivityApplyHistory>()
                    .eq(ActivityApplyHistory::getActivityApplyId, apply.getId())
                    .eq(ActivityApplyHistory::getHistoryStatus, ActivityApplyConstants.HISTORY_PENDING)
                    .orderByDesc(ActivityApplyHistory::getCreateTime)
                    .last("LIMIT 1"));
            if (history != null) {
                history.setHistoryStatus(ActivityApplyConstants.HISTORY_APPLIED);
                activityApplyHistoryMapper.updateById(history);
                apply.setStartTime(history.getNewStartTime());
                apply.setEndTime(history.getNewEndTime());
                apply.setLocation(history.getNewLocation());
                apply.setLocationDetail(history.getNewLocationDetail());
            }
        }
        apply.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        apply.setUpdateTime(now);
        if (!updateById(apply)) {
            throwVersionConflict();
        }
    }

    private void createFlowSteps(ActivityApply apply, SysClub club, List<String> approverTypes,
                                 int flowType, LocalDateTime now) {
        int step = 1;
        for (String approverType : approverTypes) {
            Long approverUserId = ActivityApproverHelper.resolveApproverUserId(
                    approverType, club, sysUserRoleMapper, sysRoleMapper, sysCollegeMapper);
            Long roleId = ActivityApproverHelper.resolveRoleIdForApprover(approverType, sysRoleMapper);

            ActivityApproveFlow flow = new ActivityApproveFlow();
            flow.setActivityId(apply.getId());
            flow.setFlowType(flowType);
            flow.setStep(step);
            flow.setApproveRoleId(roleId);
            flow.setApproveUserId(approverUserId);
            flow.setCreateTime(now);
            if (step == 1) {
                flow.setStepEnterTime(now);
            }
            activityApproveFlowMapper.insert(flow);
            step++;
        }
    }

    private void maybeAdjustLevel(ActivityApply apply, SysClub club, Long userId, Integer newLevel) {
        if (newLevel == null || Objects.equals(newLevel, apply.getActivityLevel())) {
            return;
        }
        if (!Objects.equals(club.getAdvisorId(), userId)) {
            return;
        }
        if (apply.getLevelAdjustLocked() != null && apply.getLevelAdjustLocked() == 1) {
            throw new EIException(ErrorConfig.ACT_LEVEL_ADJUST_LOCKED_CODE, ErrorConfig.ACT_LEVEL_ADJUST_LOCKED_MSG);
        }
        validateActivityLevel(newLevel);
        apply.setActivityLevel(newLevel);
        apply.setLevelAdjustLocked(1);
    }

    private void saveChangeHistory(ActivityApply apply, ActivityChangeDTO dto, int historyStatus, LocalDateTime now) {
        ActivityApplyHistory history = new ActivityApplyHistory();
        history.setActivityApplyId(apply.getId());
        history.setActivityNo(apply.getActivityNo());
        history.setClubId(apply.getClubId());
        history.setActivityName(apply.getActivityName());
        history.setCategoryId(apply.getCategoryId());
        history.setActivityType(apply.getActivityType());
        history.setStartTime(apply.getStartTime());
        history.setEndTime(apply.getEndTime());
        history.setLocation(apply.getLocation());
        history.setLocationDetail(apply.getLocationDetail());
        history.setExpectedPeople(apply.getExpectedPeople());
        history.setBudget(apply.getBudget());
        history.setActivityContent(apply.getActivityContent());
        history.setSafetyPlan(apply.getSafetyPlan());
        history.setAttachment(apply.getAttachment());
        history.setActivityLevel(apply.getActivityLevel());
        history.setHistoryReason(dto.getChangeReason());
        history.setNewStartTime(dto.getStartTime());
        history.setNewEndTime(dto.getEndTime());
        history.setNewLocation(dto.getLocation());
        history.setNewLocationDetail(dto.getLocationDetail());
        history.setHistoryStatus(historyStatus);
        history.setCreateTime(now);
        activityApplyHistoryMapper.insert(history);
    }

    private void markLatestHistoryRejected(Long activityId) {
        ActivityApplyHistory history = activityApplyHistoryMapper.selectOne(new LambdaQueryWrapper<ActivityApplyHistory>()
                .eq(ActivityApplyHistory::getActivityApplyId, activityId)
                .eq(ActivityApplyHistory::getHistoryStatus, ActivityApplyConstants.HISTORY_PENDING)
                .orderByDesc(ActivityApplyHistory::getCreateTime)
                .last("LIMIT 1"));
        if (history != null) {
            history.setHistoryStatus(ActivityApplyConstants.HISTORY_REJECTED);
            activityApplyHistoryMapper.updateById(history);
        }
    }

    private ActivityApproveFlow requireCurrentFlow(ActivityApply apply, int flowType) {
        ActivityApproveFlow flow = activityApproveFlowMapper.selectOne(new LambdaQueryWrapper<ActivityApproveFlow>()
                .eq(ActivityApproveFlow::getActivityId, apply.getId())
                .eq(ActivityApproveFlow::getFlowType, flowType)
                .eq(ActivityApproveFlow::getStep, apply.getCurrentApproveStep()));
        if (flow == null) {
            throw new EIException(ErrorConfig.ACT_STATUS_INVALID_CODE, ErrorConfig.ACT_STATUS_INVALID_MSG);
        }
        if (flow.getApproveResult() != null) {
            throw new EIException(ErrorConfig.ACT_STATUS_INVALID_CODE, "当前步骤已处理");
        }
        return flow;
    }

    private String resolveCurrentApproverName(ActivityApply apply) {
        if (apply.getApproveStatus() == null) {
            return null;
        }
        if (apply.getApproveStatus() != ActivityApplyConstants.STATUS_PENDING
                && apply.getApproveStatus() != ActivityApplyConstants.STATUS_IN_PROGRESS
                && apply.getApproveStatus() != ActivityApplyConstants.STATUS_CHANGE_PENDING) {
            return null;
        }
        int flowType = apply.getApproveStatus() == ActivityApplyConstants.STATUS_CHANGE_PENDING
                ? ActivityApplyConstants.FLOW_TYPE_CHANGE
                : ActivityApplyConstants.FLOW_TYPE_NORMAL;
        ActivityApproveFlow flow = activityApproveFlowMapper.selectOne(new LambdaQueryWrapper<ActivityApproveFlow>()
                .eq(ActivityApproveFlow::getActivityId, apply.getId())
                .eq(ActivityApproveFlow::getFlowType, flowType)
                .eq(ActivityApproveFlow::getStep, apply.getCurrentApproveStep()));
        if (flow == null || flow.getApproveUserId() == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(flow.getApproveUserId());
        return user != null ? user.getRealName() : null;
    }

    private ActivityApply requireApply(Long id) {
        ActivityApply apply = getById(id);
        if (apply == null) {
            throw new EIException(ErrorConfig.ACT_APPLY_NOT_FOUND_CODE, ErrorConfig.ACT_APPLY_NOT_FOUND_MSG);
        }
        return apply;
    }

    private SysClub requireNormalClub(Long clubId) {
        SysClub club = sysClubMapper.selectById(clubId);
        if (club == null || club.getStatus() == null || club.getStatus() != ClubApplyConstants.CLUB_STATUS_NORMAL) {
            throw new EIException(ErrorConfig.ACT_CLUB_NOT_FOUND_CODE, ErrorConfig.ACT_CLUB_NOT_FOUND_MSG);
        }
        return club;
    }

    private void assertVersion(ActivityApply apply, Integer version) {
        if (version == null || !Objects.equals(apply.getVersion(), version)) {
            throw new EIException(ErrorConfig.ACT_VERSION_CONFLICT_CODE, ErrorConfig.ACT_VERSION_CONFLICT_MSG);
        }
    }

    private void throwVersionConflict() {
        throw new EIException(ErrorConfig.ACT_VERSION_CONFLICT_CODE, ErrorConfig.ACT_VERSION_CONFLICT_MSG);
    }

    private void validateActivityLevel(Integer level) {
        if (level == null || (level != ActivityApplyConstants.LEVEL_COLLEGE && level != ActivityApplyConstants.LEVEL_SCHOOL)) {
            throw new EIException(ErrorConfig.ACT_LEVEL_INVALID_CODE, ErrorConfig.ACT_LEVEL_INVALID_MSG);
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new EIException(ErrorConfig.ACT_TIME_INVALID_CODE, ErrorConfig.ACT_TIME_INVALID_MSG);
        }
    }

    // ====================== 门户方法 ======================

    @Override
    public Page<PortalActivityListVO> portalList(int page, int size, LocalDateTime freezeTime) {
        LocalDateTime now = LocalDateTime.now();
        if (freezeTime == null) {
            freezeTime = now;
        } else if (freezeTime.isAfter(now.plusMinutes(5))) {
            freezeTime = now;
        }

        LocalDateTime windowStart = freezeTime.minusMonths(6);
        LocalDateTime windowEnd = freezeTime.plusMonths(2);

        Page<ActivityApply> pageParam = new Page<>(page, size);
        IPage<ActivityApply> activityPage = page(pageParam, new LambdaQueryWrapper<ActivityApply>()
                .eq(ActivityApply::getApproveStatus, ActivityApplyConstants.STATUS_APPROVED)
                .ge(ActivityApply::getStartTime, windowStart)
                .le(ActivityApply::getStartTime, windowEnd)
                .orderByAsc(ActivityApply::getStartTime));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<PortalActivityListVO> voList = activityPage.getRecords().stream().map(activity -> {
            PortalActivityListVO vo = new PortalActivityListVO();
            vo.setActivityNo(activity.getActivityNo());
            vo.setActivityName(activity.getActivityName());
            vo.setStartTime(activity.getStartTime() != null ? activity.getStartTime().format(dtf) : null);
            vo.setEndTime(activity.getEndTime() != null ? activity.getEndTime().format(dtf) : null);
            vo.setLocation(activity.getLocation());
            vo.setActivityLevel(activity.getActivityLevel());
            vo.setCoverImage(activity.getCoverImage());

            // 联查社团
            if (activity.getClubId() != null) {
                SysClub club = sysClubMapper.selectById(activity.getClubId());
                if (club != null) {
                    vo.setClubName(club.getClubName());
                    vo.setClubCategoryName(club.getCategory());
                }
            }
            // 联查活动分类
            if (activity.getCategoryId() != null) {
                ActivityCategory category = activityCategoryMapper.selectById(activity.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        Page<PortalActivityListVO> result = new Page<>(page, size);
        result.setTotal(activityPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional
    public PortalActivityDetailVO portalDetail(String activityNo) {
        ActivityApply activity = getOne(new LambdaQueryWrapper<ActivityApply>()
                .eq(ActivityApply::getActivityNo, activityNo));
        if (activity == null) {
            throw new EIException(ErrorConfig.ACT_APPLY_NOT_FOUND_CODE, ErrorConfig.ACT_APPLY_NOT_FOUND_MSG);
        }

        // 防篡改校验
        checkActivityNoTamper(activity);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        PortalActivityDetailVO vo = new PortalActivityDetailVO();
        vo.setActivityNo(activity.getActivityNo());
        vo.setActivityName(activity.getActivityName());
        vo.setStartTime(activity.getStartTime() != null ? activity.getStartTime().format(dtf) : null);
        vo.setEndTime(activity.getEndTime() != null ? activity.getEndTime().format(dtf) : null);
        vo.setLocation(activity.getLocation());
        vo.setActivityLevel(activity.getActivityLevel());
        vo.setCoverImage(activity.getCoverImage());
        vo.setContent(activity.getActivityContent());
        vo.setOrganizerNote(activity.getOrganizerNote());

        // 联查社团
        if (activity.getClubId() != null) {
            SysClub club = sysClubMapper.selectById(activity.getClubId());
            if (club != null) {
                vo.setClubName(club.getClubName());
                vo.setClubCategoryName(club.getCategory());
            }
        }
        // 联查活动分类
        if (activity.getCategoryId() != null) {
            ActivityCategory category = activityCategoryMapper.selectById(activity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        // 联查签到配置（仅公开字段）
        ActivitySignConfig signConfig = activitySignConfigMapper.selectOne(
                new LambdaQueryWrapper<ActivitySignConfig>()
                        .eq(ActivitySignConfig::getActivityId, activity.getId()));
        if (signConfig != null && signConfig.getEnabled() != null && signConfig.getEnabled() == 1) {
            vo.setSignMode(signConfig.getSignMode());
            vo.setSignStartTime(signConfig.getSignStartTime() != null ? signConfig.getSignStartTime().format(dtf) : null);
            vo.setSignEndTime(signConfig.getSignEndTime() != null ? signConfig.getSignEndTime().format(dtf) : null);
            vo.setCheckoutEnabled(signConfig.getEnableCheckout() != null && signConfig.getEnableCheckout() == 1);
            // 运行时计算 signAvailable
            LocalDateTime now = LocalDateTime.now();
            vo.setSignAvailable(signConfig.getSignStartTime() != null && signConfig.getSignEndTime() != null
                    && now.isAfter(signConfig.getSignStartTime())
                    && now.isBefore(signConfig.getSignEndTime()));
        }
        return vo;
    }

    /**
     * 防篡改校验：从 activityNo 提取日期，与 create_time 比对
     */
    private void checkActivityNoTamper(ActivityApply activity) {
        String activityNo = activity.getActivityNo();
        if (activityNo == null || activityNo.length() < 19) {
            return;
        }
        try {
            // activityNo 格式：{2位前缀}{yyyyMMddHHmm}{5位随机}（19位），如 WH20260718143082739
            String dateStr = activityNo.substring(2, 14);
            LocalDateTime noDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            LocalDateTime createMinute = activity.getCreateTime() != null
                    ? activity.getCreateTime().withSecond(0).withNano(0)
                    : null;

            if (createMinute != null && !noDate.equals(createMinute)) {
                // 篡改：封锁活动
                activity.setApproveStatus(ActivityApplyConstants.STATUS_BLOCKED);
                activity.setUpdateTime(LocalDateTime.now());
                updateById(activity);

                // 通知管理员
                noticeAutoPublisher.publishBlockedNotice(activity);

                throw new EIException(ErrorConfig.ACT_CODE_TAMPER_CODE, ErrorConfig.ACT_CODE_TAMPER_MSG);
            }
        } catch (Exception e) {
            if (e instanceof EIException) {
                throw (EIException) e;
            }
            log.warn("活动编号 {} 日期解析失败", activityNo, e);
        }
    }
}
