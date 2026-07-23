package untiy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import untiy.entity.ActivityApply;
import untiy.entity.ActivitySign;
import untiy.entity.ActivitySignConfig;
import untiy.entity.ActivitySignMakeup;
import untiy.entity.SysClub;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.SysRole;
import untiy.entity.constants.ActivitySignConstants;
import untiy.entity.dto.AdminSignDTO;
import untiy.entity.dto.MakeupApplyDTO;
import untiy.entity.dto.MakeupApproveDTO;
import untiy.entity.dto.SignActionDTO;
import untiy.entity.vo.SignStatsVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.ActivitySignConfigMapper;
import untiy.mapper.ActivitySignMakeupMapper;
import untiy.mapper.ActivitySignMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.UserSecurityHelper;
import untiy.service.AuthorService;
import untiy.service.impl.ActivitySignServiceImpl;
import untiy.testsupport.TestDataFactory;
import untiy.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * P0：签到核心边界（时间窗口 / 重复 / 冲突 / 签退 / 代签 / 统计）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivitySignServiceImplTest {

    private static final String ACTIVITY_NO = "WH20260718143000001";
    private static final Long ACTIVITY_ID = 10L;
    private static final Long CLUB_ID = 20L;
    private static final Long USER_ID = 100L;

    @Mock private ActivitySignMapper activitySignMapper;
    @Mock private ActivitySignConfigMapper activitySignConfigMapper;
    @Mock private ActivitySignMakeupMapper activitySignMakeupMapper;
    @Mock private ActivityApplyMapper activityApplyMapper;
    @Mock private SysClubMapper sysClubMapper;
    @Mock private SysCollegeMapper sysCollegeMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private AuthorService authorService;

    @InjectMocks
    private ActivitySignServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;
    private ActivityApply apply;
    private ActivitySignConfig config;
    private LoginUserDetails loginUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", activitySignMapper);

        apply = TestDataFactory.approvedActivity(ACTIVITY_ID, ACTIVITY_NO, CLUB_ID);
        LocalDateTime now = LocalDateTime.now();
        config = TestDataFactory.signConfig(ACTIVITY_ID, ACTIVITY_NO,
                now.minusMinutes(30), now.plusMinutes(30), true);

        SysUser sysUser = TestDataFactory.student(USER_ID, "u100");
        loginUser = new LoginUserDetails(sysUser, authorService, Level.STUDENT, CLUB_ID, null);

        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(loginUser);

        when(activityApplyMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(apply);
        when(activitySignConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(config);
    }

    @AfterEach
    void tearDown() {
        if (securityUtils != null) {
            securityUtils.close();
        }
    }

    @Test
    @DisplayName("sign: 窗口内正常签到")
    void sign_withinWindow_success() {
        when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(activitySignMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(activitySignMapper.insert(any(ActivitySign.class))).thenReturn(1);

        SignActionDTO dto = new SignActionDTO();
        dto.setSignMethod(ActivitySignConstants.MODE_LOCATION);
        dto.setLatitude(BigDecimal.valueOf(30.1));
        dto.setLongitude(BigDecimal.valueOf(120.1));

        service.sign(ACTIVITY_NO, dto);

        ArgumentCaptor<ActivitySign> captor = ArgumentCaptor.forClass(ActivitySign.class);
        verify(activitySignMapper).insert(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(ACTIVITY_ID);
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("sign: 签到开始前1秒拒绝")
    void sign_beforeWindow_throws() {
        LocalDateTime now = LocalDateTime.now();
        config.setSignStartTime(now.plusSeconds(5));
        config.setSignEndTime(now.plusHours(1));

        SignActionDTO dto = new SignActionDTO();
        dto.setSignMethod(ActivitySignConstants.MODE_LOCATION);

        assertThatThrownBy(() -> service.sign(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_WINDOW_CLOSED_CODE);
    }

    @Test
    @DisplayName("sign: 签到结束后拒绝")
    void sign_afterWindow_throws() {
        LocalDateTime now = LocalDateTime.now();
        config.setSignStartTime(now.minusHours(2));
        config.setSignEndTime(now.minusSeconds(1));

        SignActionDTO dto = new SignActionDTO();
        dto.setSignMethod(ActivitySignConstants.MODE_LOCATION);

        assertThatThrownBy(() -> service.sign(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_WINDOW_CLOSED_CODE);
    }

    @Test
    @DisplayName("sign: 重复签到拒绝")
    void sign_duplicate_throws() {
        ActivitySign existing = new ActivitySign();
        existing.setId(1L);
        existing.setActivityId(ACTIVITY_ID);
        existing.setUserId(USER_ID);
        when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        SignActionDTO dto = new SignActionDTO();
        dto.setSignMethod(ActivitySignConstants.MODE_LOCATION);

        assertThatThrownBy(() -> service.sign(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_ALREADY_SIGNED_CODE);
    }

    @Test
    @DisplayName("sign: 同时段其他活动冲突")
    void sign_timeConflict_throws() {
        when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ActivitySign otherSign = new ActivitySign();
        otherSign.setActivityId(99L);
        otherSign.setUserId(USER_ID);
        when(activitySignMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(otherSign));

        ActivityApply other = TestDataFactory.approvedActivity(99L, "XS20260718143000002", CLUB_ID);
        other.setStartTime(apply.getStartTime().plusMinutes(5));
        other.setEndTime(apply.getEndTime().plusMinutes(5));
        when(activityApplyMapper.selectById(99L)).thenReturn(other);

        SignActionDTO dto = new SignActionDTO();
        dto.setSignMethod(ActivitySignConstants.MODE_LOCATION);

        assertThatThrownBy(() -> service.sign(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_CONFLICT_CODE);
    }

    @Test
    @DisplayName("sign: 活动未审批通过")
    void sign_activityNotApproved_throws() {
        apply.setApproveStatus(1);
        when(activityApplyMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(apply);

        SignActionDTO dto = new SignActionDTO();
        assertThatThrownBy(() -> service.sign(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_ACTIVITY_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("checkout: 未启用签退")
    void checkout_disabled_throws() {
        config.setEnableCheckout(0);
        assertThatThrownBy(() -> service.checkout(ACTIVITY_NO))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_CHECKOUT_DISABLED_CODE);
    }

    @Test
    @DisplayName("checkout: 未签到先签退")
    void checkout_notSigned_throws() {
        when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        assertThatThrownBy(() -> service.checkout(ACTIVITY_NO))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_NOT_SIGNED_CODE);
    }

    @Test
    @DisplayName("checkout: 正常签退")
    void checkout_success() {
        ActivitySign record = new ActivitySign();
        record.setId(1L);
        record.setActivityId(ACTIVITY_ID);
        record.setUserId(USER_ID);
        record.setSignTime(LocalDateTime.now().minusMinutes(5));
        when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(activitySignMapper.updateById(any(ActivitySign.class))).thenReturn(1);

        service.checkout(ACTIVITY_NO);

        verify(activitySignMapper).updateById(argThat(s -> s.getCheckoutTime() != null));
    }

    @Test
    @DisplayName("adminSign: 目标用户禁用")
    void adminSign_disabledUser_throws() {
        SysClub club = TestDataFactory.club(CLUB_ID, "测社团", "文化体育类", 1L);
        when(sysClubMapper.selectById(CLUB_ID)).thenReturn(club);

        LoginUserDetails admin = new LoginUserDetails(
                TestDataFactory.user(1L, "admin", 2, 1), authorService, Level.ADMIN, null, null);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(admin);

        when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        SysUser disabled = TestDataFactory.student(200L, "disabled_user");
        disabled.setStatus(0);

        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.requireInScopeByUsername(eq(sysUserMapper), eq("disabled_user")))
                    .thenReturn(disabled);
            helper.when(() -> UserSecurityHelper.assertUserEnabled(disabled))
                    .thenThrow(new EIException(ErrorConfig.USER_DISABLED_CODE, ErrorConfig.USER_DISABLED_MSG));

            AdminSignDTO dto = new AdminSignDTO();
            dto.setUsername("disabled_user");

            assertThatThrownBy(() -> service.adminSign(ACTIVITY_NO, dto))
                    .isInstanceOf(EIException.class)
                    .extracting(e -> ((EIException) e).getCode())
                    .isEqualTo(ErrorConfig.USER_DISABLED_CODE);
        }
    }

    @Test
    @DisplayName("stats: 0人签到")
    void stats_zeroSigned() {
        SysClub club = TestDataFactory.club(CLUB_ID, "测社团", "文化体育类", 1L);
        when(sysClubMapper.selectById(CLUB_ID)).thenReturn(club);
        LoginUserDetails admin = new LoginUserDetails(
                TestDataFactory.user(1L, "admin", 2, 1), authorService, Level.ADMIN, null, null);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(admin);

        when(activitySignMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        SignStatsVO vo = service.stats(ACTIVITY_NO);
        assertThat(vo.getActivityNo()).isEqualTo(ACTIVITY_NO);
        assertThat(vo.getSignedCount()).isZero();
        assertThat(vo.getUnsignedCount()).isEqualTo(10);
    }

    private void stubClubPresident(Long userId, Long clubId) {
        SysRole role = TestDataFactory.presidentRole(5L);
        SysUserRole ur = TestDataFactory.userRole(1L, userId, 5L,
                untiy.entity.constants.ClubApplyConstants.SCOPE_TYPE_CLUB, clubId);
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(ur));
        when(sysRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(role));
    }

    @Test
    @DisplayName("applyMakeup: 非社长禁止")
    void applyMakeup_notPresident_throws() {
        SysClub club = TestDataFactory.club(CLUB_ID, "测社团", "文化体育类", 1L);
        when(sysClubMapper.selectById(CLUB_ID)).thenReturn(club);
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(sysRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        MakeupApplyDTO dto = new MakeupApplyDTO();
        dto.setUsername("u200");
        dto.setReasonType(1);
        dto.setReasonDetail("迟到");

        assertThatThrownBy(() -> service.applyMakeup(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_MAKEUP_PRESIDENT_ONLY_CODE);
    }

    @Test
    @DisplayName("applyMakeup: 超过补签截止日")
    void applyMakeup_expired_throws() {
        apply.setEndTime(LocalDateTime.now().minusDays(3));
        SysClub club = TestDataFactory.club(CLUB_ID, "测社团", "文化体育类", 1L);
        when(sysClubMapper.selectById(CLUB_ID)).thenReturn(club);
        stubClubPresident(USER_ID, CLUB_ID);

        LoginUserDetails president = new LoginUserDetails(
                TestDataFactory.student(USER_ID, "u100"), authorService, Level.CLUB_LEADER, CLUB_ID, null);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(president);

        MakeupApplyDTO dto = new MakeupApplyDTO();
        dto.setUsername("u200");
        dto.setReasonType(1);
        dto.setReasonDetail("迟到");

        assertThatThrownBy(() -> service.applyMakeup(ACTIVITY_NO, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_MAKEUP_EXPIRED_CODE);
    }

    @Test
    @DisplayName("applyMakeup: 目标已签到")
    void applyMakeup_alreadySigned_throws() {
        SysClub club = TestDataFactory.club(CLUB_ID, "测社团", "文化体育类", 1L);
        when(sysClubMapper.selectById(CLUB_ID)).thenReturn(club);
        stubClubPresident(USER_ID, CLUB_ID);

        LoginUserDetails president = new LoginUserDetails(
                TestDataFactory.student(USER_ID, "u100"), authorService, Level.CLUB_LEADER, CLUB_ID, null);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(president);

        SysUser target = TestDataFactory.student(200L, "u200");
        ActivitySign existing = new ActivitySign();
        existing.setId(1L);
        existing.setActivityId(ACTIVITY_ID);
        existing.setUserId(200L);

        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.requireInScopeByUsername(eq(sysUserMapper), eq("u200")))
                    .thenReturn(target);
            helper.when(() -> UserSecurityHelper.assertUserEnabled(target)).thenAnswer(inv -> null);
            when(activitySignMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setUsername("u200");
            dto.setReasonType(1);
            dto.setReasonDetail("迟到");

            assertThatThrownBy(() -> service.applyMakeup(ACTIVITY_NO, dto))
                    .isInstanceOf(EIException.class)
                    .extracting(e -> ((EIException) e).getCode())
                    .isEqualTo(ErrorConfig.SIGN_ALREADY_SIGNED_CODE);
        }
    }

    @Test
    @DisplayName("approveMakeup: 申请不存在")
    void approveMakeup_notFound_throws() {
        when(activitySignMakeupMapper.selectById(999L)).thenReturn(null);
        MakeupApproveDTO dto = new MakeupApproveDTO();
        dto.setApproved(true);
        dto.setOpinion("同意");
        assertThatThrownBy(() -> service.approveMakeup(999L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_MAKEUP_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("approveMakeup: 非当前审批人")
    void approveMakeup_notApprover_throws() {
        ActivitySignMakeup makeup = new ActivitySignMakeup();
        makeup.setId(1L);
        makeup.setActivityId(ACTIVITY_ID);
        makeup.setStatus(ActivitySignConstants.MAKEUP_PENDING);
        makeup.setApproveUserId(999L);
        when(activitySignMakeupMapper.selectById(1L)).thenReturn(makeup);

        MakeupApproveDTO dto = new MakeupApproveDTO();
        dto.setApproved(true);
        dto.setOpinion("同意");

        assertThatThrownBy(() -> service.approveMakeup(1L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.SIGN_MAKEUP_NOT_APPROVER_CODE);
    }

    @Test
    @DisplayName("approveMakeup: 院级活动一步通过并写入补签记录")
    void approveMakeup_collegeLevel_createsSign() {
        ActivitySignMakeup makeup = new ActivitySignMakeup();
        makeup.setId(1L);
        makeup.setActivityId(ACTIVITY_ID);
        makeup.setUserId(200L);
        makeup.setStatus(ActivitySignConstants.MAKEUP_PENDING);
        makeup.setCurrentStep(1);
        makeup.setApproveUserId(USER_ID);
        when(activitySignMakeupMapper.selectById(1L)).thenReturn(makeup);
        when(activitySignMakeupMapper.updateById(any(ActivitySignMakeup.class))).thenReturn(1);
        when(activityApplyMapper.selectById(ACTIVITY_ID)).thenReturn(apply);

        SysClub club = TestDataFactory.club(CLUB_ID, "测社团", "文化体育类", 1L);
        when(sysClubMapper.selectById(CLUB_ID)).thenReturn(club);
        when(activitySignMapper.insert(any(ActivitySign.class))).thenReturn(1);

        MakeupApproveDTO dto = new MakeupApproveDTO();
        dto.setApproved(true);
        dto.setOpinion("同意补签");

        service.approveMakeup(1L, dto);

        ArgumentCaptor<ActivitySign> captor = ArgumentCaptor.forClass(ActivitySign.class);
        verify(activitySignMapper).insert(captor.capture());
        assertThat(captor.getValue().getSignType()).isEqualTo(ActivitySignConstants.TYPE_MAKEUP);
        assertThat(captor.getValue().getUserId()).isEqualTo(200L);
        verify(activitySignMakeupMapper).updateById(argThat(m ->
                m.getStatus() != null && m.getStatus() == ActivitySignConstants.MAKEUP_APPROVED));
    }
}
