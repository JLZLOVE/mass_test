package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.*;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.ActivitySignConstants;
import untiy.entity.dto.*;
import untiy.entity.vo.SignRecordVO;
import untiy.entity.vo.SignStatsVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.*;
import untiy.security.ActivitySignHelper;
import untiy.security.LoginUserDetails;
import untiy.security.UserSecurityHelper;
import untiy.service.ActivitySignService;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivitySignServiceImpl extends ServiceImpl<ActivitySignMapper, ActivitySign>
        implements ActivitySignService {

    @Autowired
    private ActivitySignConfigMapper activitySignConfigMapper;

    @Autowired
    private ActivitySignMakeupMapper activitySignMakeupMapper;

    @Autowired
    private ActivityApplyMapper activityApplyMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Transactional
    @Override
    public void saveConfig(SignConfigDTO dto) {
        ActivityApply apply = requireApprovedActivityByNo(dto.getActivityNo());
        assertSignConfigTimes(dto);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        ActivitySignHelper.assertActivityOwnerOrAdvisor(apply, club, user.getUserId(), user.getEffectiveLevel());

        ActivitySignConfig existing = findConfig(apply.getId());
        if (existing != null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "签到已配置，请使用更新接口");
        }
        insertConfig(dto, apply);
    }

    @Transactional
    @Override
    public void updateConfig(SignConfigDTO dto) {
        ActivityApply apply = requireApprovedActivityByNo(dto.getActivityNo());
        assertSignConfigTimes(dto);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        ActivitySignHelper.assertActivityOwnerOrAdvisor(apply, club, user.getUserId(), user.getEffectiveLevel());

        ActivitySignConfig config = findConfig(apply.getId());
        if (config == null) {
            insertConfig(dto, apply);
            return;
        }
        fillConfig(config, dto, apply);
        config.setUpdateTime(LocalDateTime.now());
        activitySignConfigMapper.updateById(config);
    }

    @Override
    public ActivitySignConfig getConfig(String activityNo) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        ActivitySignConfig config = findConfig(apply.getId());
        if (config == null || config.getEnabled() == null || config.getEnabled() != 1) {
            throw new EIException(ErrorConfig.SIGN_CONFIG_NOT_FOUND_CODE, ErrorConfig.SIGN_CONFIG_NOT_FOUND_MSG);
        }
        enrichConfigActivityNo(config, apply);
        return config;
    }

    @Transactional
    @Override
    public void sign(String activityNo, SignActionDTO dto) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        ActivitySignConfig config = getConfig(activityNo);
        assertSignWindow(config);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        assertNotDuplicate(apply.getId(), user.getUserId());
        assertNoTimeConflict(user.getUserId(), apply);

        int method = dto.getSignMethod() != null ? dto.getSignMethod() : ActivitySignConstants.MODE_LOCATION;
        int signType = resolveSignType(config, method);
        validateSignMethod(config, method, dto);

        LocalDateTime now = LocalDateTime.now();
        ActivitySign record = buildSignRecord(apply.getId(), user.getUserId(), signType, now, dto.getAddress(),
                dto.getLatitude(), dto.getLongitude(), null, null);
        applyLateFlag(record, apply, now);
        save(record);
        log.info("用户 {} 活动 {} 签到成功", user.getUserId(), activityNo);
    }

    @Transactional
    @Override
    public void adminSign(String activityNo, AdminSignDTO dto) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        getConfig(activityNo);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        ActivitySignHelper.assertActivityOwnerOrAdvisor(apply, club, user.getUserId(), user.getEffectiveLevel());

        SysUser target = UserSecurityHelper.requireInScopeByUsername(sysUserMapper, dto.getUsername());
        UserSecurityHelper.assertUserEnabled(target);
        assertNotDuplicate(apply.getId(), target.getId());

        LocalDateTime now = LocalDateTime.now();
        ActivitySign record = buildSignRecord(apply.getId(), target.getId(), ActivitySignConstants.TYPE_MANUAL,
                now, dto.getAddress(), null, null, user.getUserId(), null);
        applyLateFlag(record, apply, now);
        save(record);
    }

    @Transactional
    @Override
    public void checkout(String activityNo) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        ActivitySignConfig config = getConfig(activityNo);
        if (config.getEnableCheckout() == null || config.getEnableCheckout() != 1) {
            throw new EIException(ErrorConfig.SIGN_CHECKOUT_DISABLED_CODE, ErrorConfig.SIGN_CHECKOUT_DISABLED_MSG);
        }
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        ActivitySign record = findSignRecord(apply.getId(), user.getUserId());
        if (record == null) {
            throw new EIException(ErrorConfig.SIGN_NOT_SIGNED_CODE, ErrorConfig.SIGN_NOT_SIGNED_MSG);
        }
        if (record.getCheckoutTime() != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        assertCheckoutTimeValid(record.getSignTime(), now);
        record.setCheckoutTime(now);
        if (apply.getEndTime() != null && now.isBefore(apply.getEndTime())) {
            record.setIsEarlyLeave(1);
            record.setSignStatus(ActivitySignConstants.STATUS_EARLY_LEAVE);
        }
        updateById(record);
    }

    @Transactional
    @Override
    public Long applyMakeup(String activityNo, MakeupApplyDTO dto) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        ActivitySignHelper.assertClubPresident(sysUserRoleMapper, sysRoleMapper, user.getUserId(), apply.getClubId());

        assertMakeupDeadline(apply);
        SysUser target = UserSecurityHelper.requireInScopeByUsername(sysUserMapper, dto.getUsername());
        UserSecurityHelper.assertUserEnabled(target);
        if (findSignRecord(apply.getId(), target.getId()) != null) {
            throw new EIException(ErrorConfig.SIGN_ALREADY_SIGNED_CODE, ErrorConfig.SIGN_ALREADY_SIGNED_MSG);
        }

        LocalDateTime now = LocalDateTime.now();
        ActivitySignMakeup makeup = new ActivitySignMakeup();
        makeup.setActivityId(apply.getId());
        makeup.setUserId(target.getId());
        makeup.setApplicantId(user.getUserId());
        makeup.setReasonType(dto.getReasonType());
        makeup.setReasonDetail(dto.getReasonDetail());
        makeup.setAttachment(dto.getAttachment());
        makeup.setStatus(ActivitySignConstants.MAKEUP_PENDING);
        makeup.setCurrentStep(1);
        makeup.setApproveUserId(ActivitySignHelper.resolveMakeupApprover(1, apply, club, sysCollegeMapper));
        makeup.setCreateTime(now);
        makeup.setUpdateTime(now);
        activitySignMakeupMapper.insert(makeup);
        return makeup.getId();
    }

    @Transactional
    @Override
    public void approveMakeup(Long applyId, MakeupApproveDTO dto) {
        ActivitySignMakeup makeup = activitySignMakeupMapper.selectById(applyId);
        if (makeup == null) {
            throw new EIException(ErrorConfig.SIGN_MAKEUP_NOT_FOUND_CODE, ErrorConfig.SIGN_MAKEUP_NOT_FOUND_MSG);
        }
        if (makeup.getStatus() == null || makeup.getStatus() != ActivitySignConstants.MAKEUP_PENDING) {
            throw new EIException(ErrorConfig.SIGN_MAKEUP_NOT_FOUND_CODE, "补签申请已处理");
        }
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (!Objects.equals(makeup.getApproveUserId(), user.getUserId())) {
            throw new EIException(ErrorConfig.SIGN_MAKEUP_NOT_APPROVER_CODE, ErrorConfig.SIGN_MAKEUP_NOT_APPROVER_MSG);
        }

        ActivityApply apply = requireApprovedActivity(makeup.getActivityId());
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        LocalDateTime now = LocalDateTime.now();
        makeup.setApproveOpinion(dto.getOpinion());

        if (!Boolean.TRUE.equals(dto.getApproved())) {
            makeup.setStatus(ActivitySignConstants.MAKEUP_REJECTED);
            makeup.setUpdateTime(now);
            activitySignMakeupMapper.updateById(makeup);
            return;
        }

        int totalSteps = ActivitySignHelper.makeupTotalSteps(apply);
        if (makeup.getCurrentStep() < totalSteps) {
            makeup.setCurrentStep(makeup.getCurrentStep() + 1);
            makeup.setApproveUserId(ActivitySignHelper.resolveMakeupApprover(makeup.getCurrentStep(), apply, club, sysCollegeMapper));
            makeup.setUpdateTime(now);
            activitySignMakeupMapper.updateById(makeup);
            return;
        }

        makeup.setStatus(ActivitySignConstants.MAKEUP_APPROVED);
        makeup.setUpdateTime(now);
        activitySignMakeupMapper.updateById(makeup);

        ActivitySign record = buildSignRecord(makeup.getActivityId(), makeup.getUserId(),
                ActivitySignConstants.TYPE_MAKEUP, now, apply.getLocation(), null, null, null, user.getUserId());
        applyLateFlag(record, apply, now);
        save(record);
    }

    @Override
    public SignStatsVO stats(String activityNo) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        ActivitySignHelper.assertActivityOwnerOrAdvisor(apply, club, user.getUserId(), user.getEffectiveLevel());

        List<ActivitySign> signs = list(new LambdaQueryWrapper<ActivitySign>().eq(ActivitySign::getActivityId, apply.getId()));
        int expected = apply.getExpectedPeople() != null ? apply.getExpectedPeople() : signs.size();
        long signed = signs.size();
        long late = signs.stream().filter(s -> s.getIsLate() != null && s.getIsLate() == 1).count();
        long early = signs.stream().filter(s -> s.getIsEarlyLeave() != null && s.getIsEarlyLeave() == 1).count();

        SignStatsVO vo = new SignStatsVO();
        vo.setActivityNo(activityNo);
        vo.setExpectedCount(expected);
        vo.setSignedCount(signed);
        vo.setUnsignedCount(Math.max(0, expected - (int) signed));
        vo.setSignRate(expected > 0
                ? BigDecimal.valueOf(signed * 100.0 / expected).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setLateCount(late);
        vo.setEarlyLeaveCount(early);
        vo.setTimeDistribution(buildTimeDistribution(signs));
        vo.setQueryTime(LocalDateTime.now());
        return vo;
    }

    @Override
    public IPage<SignRecordVO> listRecords(String activityNo, Map<String, Object> param) {
        ActivityApply apply = requireApprovedActivityByNo(activityNo);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = sysClubMapper.selectById(apply.getClubId());
        ActivitySignHelper.assertActivityOwnerOrAdvisor(apply, club, user.getUserId(), user.getEffectiveLevel());

        Page<ActivitySign> page = MPUtil.getPage(param);
        IPage<ActivitySign> signPage = page(page, new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, apply.getId())
                .orderByDesc(ActivitySign::getSignTime));

        Page<SignRecordVO> voPage = new Page<>(signPage.getCurrent(), signPage.getSize(), signPage.getTotal());
        voPage.setRecords(signPage.getRecords().stream().map(this::toRecordVO).collect(Collectors.toList()));
        return voPage;
    }

    @Transactional
    @Override
    public void autoCheckoutExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<ActivityApply> ended = activityApplyMapper.selectList(new LambdaQueryWrapper<ActivityApply>()
                .eq(ActivityApply::getApproveStatus, ActivityApplyConstants.STATUS_APPROVED)
                .lt(ActivityApply::getEndTime, now));
        for (ActivityApply apply : ended) {
            ActivitySignConfig config = findConfig(apply.getId());
            if (config == null || config.getEnableCheckout() == null || config.getEnableCheckout() != 1) {
                continue;
            }
            List<ActivitySign> signs = list(new LambdaQueryWrapper<ActivitySign>()
                    .eq(ActivitySign::getActivityId, apply.getId())
                    .isNull(ActivitySign::getCheckoutTime));
            for (ActivitySign sign : signs) {
                LocalDateTime checkoutTime = resolveAutoCheckoutTime(sign.getSignTime(), apply.getEndTime());
                sign.setCheckoutTime(checkoutTime);
                updateById(sign);
            }
        }
    }

    private void insertConfig(SignConfigDTO dto, ActivityApply apply) {
        ActivitySignConfig config = new ActivitySignConfig();
        fillConfig(config, dto, apply);
        config.setEnabled(1);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        activitySignConfigMapper.insert(config);
    }

    private void fillConfig(ActivitySignConfig config, SignConfigDTO dto, ActivityApply apply) {
        config.setActivityId(apply.getId());
        config.setSignMode(dto.getSignMode());
        config.setSignStartTime(dto.getSignStartTime());
        config.setSignEndTime(dto.getSignEndTime());
        config.setSignRadius(dto.getSignRadius() != null ? dto.getSignRadius() : ActivitySignConstants.DEFAULT_RADIUS_METERS);
        config.setEnableCheckout(Boolean.TRUE.equals(dto.getEnableCheckout()) ? 1 : 0);
        config.setCenterLatitude(dto.getCenterLatitude());
        config.setCenterLongitude(dto.getCenterLongitude());
        if (config.getSignMode() == ActivitySignConstants.MODE_QR
                || config.getSignMode() == ActivitySignConstants.MODE_BOTH) {
            if (StringUtils.isBlank(config.getQrToken())) {
                config.setQrToken(UUID.randomUUID().toString().replace("-", ""));
            }
        }
    }

    private void assertSignConfigTimes(SignConfigDTO dto) {
        LocalDateTime start = dto.getSignStartTime();
        LocalDateTime end = dto.getSignEndTime();
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now)) {
            throw new EIException(ErrorConfig.SIGN_START_BEFORE_NOW_CODE, ErrorConfig.SIGN_START_BEFORE_NOW_MSG);
        }
        if (!end.isAfter(start)) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "签到开始时间必须早于签到结束时间");
        }
        if (end.isAfter(start.plusDays(ActivitySignConstants.SIGN_WINDOW_MAX_DAYS))) {
            throw new EIException(ErrorConfig.SIGN_END_TOO_LATE_CODE, ErrorConfig.SIGN_END_TOO_LATE_MSG);
        }
    }

    private void assertCheckoutTimeValid(LocalDateTime signTime, LocalDateTime checkoutTime) {
        if (signTime == null) {
            return;
        }
        if (checkoutTime.isBefore(signTime)) {
            throw new EIException(ErrorConfig.SIGN_CHECKOUT_BEFORE_SIGN_CODE, ErrorConfig.SIGN_CHECKOUT_BEFORE_SIGN_MSG);
        }
        if (checkoutTime.isAfter(signTime.plusDays(ActivitySignConstants.CHECKOUT_MAX_DAYS_AFTER_SIGN))) {
            throw new EIException(ErrorConfig.SIGN_CHECKOUT_TOO_LATE_CODE, ErrorConfig.SIGN_CHECKOUT_TOO_LATE_MSG);
        }
    }

    private LocalDateTime resolveAutoCheckoutTime(LocalDateTime signTime, LocalDateTime activityEndTime) {
        if (signTime == null) {
            return activityEndTime;
        }
        LocalDateTime maxCheckout = signTime.plusDays(ActivitySignConstants.CHECKOUT_MAX_DAYS_AFTER_SIGN);
        LocalDateTime checkoutTime = activityEndTime;
        if (checkoutTime.isBefore(signTime)) {
            checkoutTime = signTime;
        }
        if (checkoutTime.isAfter(maxCheckout)) {
            checkoutTime = maxCheckout;
        }
        return checkoutTime;
    }

    private void enrichConfigActivityNo(ActivitySignConfig config, ActivityApply apply) {
        config.setActivityNo(apply.getActivityNo());
    }

    private ActivitySign buildSignRecord(Long activityId, Long userId, int signType, LocalDateTime signTime,
                                         String address, BigDecimal lat, BigDecimal lng,
                                         Long operatorId, Long approverId) {
        ActivitySign record = new ActivitySign();
        record.setActivityId(activityId);
        record.setUserId(userId);
        record.setSignType(signType);
        record.setSignTime(signTime);
        record.setAddress(address);
        record.setLatitude(lat);
        record.setLongitude(lng);
        record.setSignStatus(ActivitySignConstants.STATUS_NORMAL);
        record.setIsLate(0);
        record.setIsEarlyLeave(0);
        record.setOperatorId(operatorId);
        record.setApproverId(approverId);
        record.setCreateTime(signTime);
        return record;
    }

    private void applyLateFlag(ActivitySign record, ActivityApply apply, LocalDateTime signTime) {
        if (apply.getStartTime() != null && signTime.isAfter(apply.getStartTime())) {
            record.setIsLate(1);
            record.setSignStatus(ActivitySignConstants.STATUS_LATE);
        }
    }

    private int resolveSignType(ActivitySignConfig config, int method) {
        if (method == ActivitySignConstants.MODE_QR) {
            return ActivitySignConstants.TYPE_QR;
        }
        return ActivitySignConstants.TYPE_LOCATION;
    }

    private void validateSignMethod(ActivitySignConfig config, int method, SignActionDTO dto) {
        int mode = config.getSignMode();
        if (method == ActivitySignConstants.MODE_LOCATION) {
            if (mode != ActivitySignConstants.MODE_LOCATION && mode != ActivitySignConstants.MODE_BOTH) {
                throw new EIException(ErrorConfig.SIGN_MODE_INVALID_CODE, ErrorConfig.SIGN_MODE_INVALID_MSG);
            }
            if (config.getCenterLatitude() != null && config.getCenterLongitude() != null) {
                double dist = ActivitySignHelper.distanceMeters(
                        dto.getLatitude(), dto.getLongitude(),
                        config.getCenterLatitude(), config.getCenterLongitude());
                int radius = config.getSignRadius() != null ? config.getSignRadius() : ActivitySignConstants.DEFAULT_RADIUS_METERS;
                if (dist > radius) {
                    throw new EIException(ErrorConfig.SIGN_LOCATION_INVALID_CODE, ErrorConfig.SIGN_LOCATION_INVALID_MSG);
                }
            }
        } else if (method == ActivitySignConstants.MODE_QR) {
            if (mode != ActivitySignConstants.MODE_QR && mode != ActivitySignConstants.MODE_BOTH) {
                throw new EIException(ErrorConfig.SIGN_MODE_INVALID_CODE, ErrorConfig.SIGN_MODE_INVALID_MSG);
            }
            if (StringUtils.isBlank(dto.getQrToken()) || !dto.getQrToken().equals(config.getQrToken())) {
                throw new EIException(ErrorConfig.SIGN_QR_INVALID_CODE, ErrorConfig.SIGN_QR_INVALID_MSG);
            }
        }
    }

    private void assertSignWindow(ActivitySignConfig config) {
        LocalDateTime now = LocalDateTime.now();
        if (config.getSignStartTime() != null && now.isBefore(config.getSignStartTime())) {
            throw new EIException(ErrorConfig.SIGN_WINDOW_CLOSED_CODE, ErrorConfig.SIGN_WINDOW_CLOSED_MSG);
        }
        if (config.getSignEndTime() != null && now.isAfter(config.getSignEndTime())) {
            throw new EIException(ErrorConfig.SIGN_WINDOW_CLOSED_CODE, ErrorConfig.SIGN_WINDOW_CLOSED_MSG);
        }
    }

    private void assertNotDuplicate(Long activityId, Long userId) {
        if (findSignRecord(activityId, userId) != null) {
            throw new EIException(ErrorConfig.SIGN_ALREADY_SIGNED_CODE, ErrorConfig.SIGN_ALREADY_SIGNED_MSG);
        }
    }

    private void assertNoTimeConflict(Long userId, ActivityApply current) {
        if (current.getStartTime() == null || current.getEndTime() == null) {
            return;
        }
        List<ActivitySign> mySigns = list(new LambdaQueryWrapper<ActivitySign>().eq(ActivitySign::getUserId, userId));
        for (ActivitySign sign : mySigns) {
            if (sign.getActivityId().equals(current.getId())) {
                continue;
            }
            ActivityApply other = activityApplyMapper.selectById(sign.getActivityId());
            if (other == null || other.getStartTime() == null || other.getEndTime() == null) {
                continue;
            }
            if (timesOverlap(current.getStartTime(), current.getEndTime(), other.getStartTime(), other.getEndTime())) {
                throw new EIException(ErrorConfig.SIGN_CONFLICT_CODE, ErrorConfig.SIGN_CONFLICT_MSG);
            }
        }
    }

    private boolean timesOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return !s1.isAfter(e2) && !s2.isAfter(e1);
    }

    private void assertMakeupDeadline(ActivityApply apply) {
        if (apply.getEndTime() == null) {
            return;
        }
        LocalDate endDate = apply.getEndTime().toLocalDate();
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate.plusDays(1))) {
            throw new EIException(ErrorConfig.SIGN_MAKEUP_EXPIRED_CODE, ErrorConfig.SIGN_MAKEUP_EXPIRED_MSG);
        }
    }

    private ActivityApply requireApprovedActivityByNo(String activityNo) {
        if (StringUtils.isBlank(activityNo)) {
            throw new EIException(ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_CODE, ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_MSG);
        }
        ActivityApply apply = activityApplyMapper.selectOne(new LambdaQueryWrapper<ActivityApply>()
                .eq(ActivityApply::getActivityNo, activityNo.trim()));
        if (apply == null || apply.getApproveStatus() == null
                || apply.getApproveStatus() != ActivityApplyConstants.STATUS_APPROVED) {
            throw new EIException(ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_CODE, ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_MSG);
        }
        return apply;
    }

    private ActivityApply requireApprovedActivity(Long activityId) {
        ActivityApply apply = activityApplyMapper.selectById(activityId);
        if (apply == null || apply.getApproveStatus() == null
                || apply.getApproveStatus() != ActivityApplyConstants.STATUS_APPROVED) {
            throw new EIException(ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_CODE, ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_MSG);
        }
        return apply;
    }

    private ActivitySignConfig findConfig(Long activityId) {
        return activitySignConfigMapper.selectOne(new LambdaQueryWrapper<ActivitySignConfig>()
                .eq(ActivitySignConfig::getActivityId, activityId));
    }

    private ActivitySign findSignRecord(Long activityId, Long userId) {
        return getOne(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityId, activityId)
                .eq(ActivitySign::getUserId, userId));
    }

    private SignRecordVO toRecordVO(ActivitySign sign) {
        SignRecordVO vo = new SignRecordVO();
        vo.setId(sign.getId());
        vo.setSignType(sign.getSignType());
        vo.setSignTime(sign.getSignTime());
        vo.setAddress(sign.getAddress());
        vo.setIsLate(sign.getIsLate());
        vo.setIsEarlyLeave(sign.getIsEarlyLeave());
        vo.setCheckoutTime(sign.getCheckoutTime());
        SysUser user = sysUserMapper.selectById(sign.getUserId());
        if (user != null) {
            vo.setRealName(user.getRealName());
            vo.setUsername(user.getUsername());
        }
        return vo;
    }

    private Map<String, Long> buildTimeDistribution(List<ActivitySign> signs) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (ActivitySign sign : signs) {
            if (sign.getSignTime() == null) {
                continue;
            }
            int minute = sign.getSignTime().getMinute();
            int bucket = minute < 30 ? 0 : 30;
            String key = String.format("%02d:%02d-%02d:%02d",
                    sign.getSignTime().getHour(), bucket,
                    sign.getSignTime().getHour(), bucket + 30);
            map.merge(key, 1L, Long::sum);
        }
        return map;
    }
}
