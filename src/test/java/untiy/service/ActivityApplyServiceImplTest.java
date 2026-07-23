package untiy.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import untiy.entity.ActivityApply;
import untiy.entity.ActivityApproveFlow;
import untiy.entity.SysClub;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.ActivityApproveDTO;
import untiy.entity.dto.ActivityCancelDTO;
import untiy.entity.dto.ActivitySubmitDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
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
import untiy.security.LoginUserDetails;
import untiy.service.impl.ActivityApplyServiceImpl;
import untiy.testsupport.TestDataFactory;
import untiy.utils.ActivityCodeGeneratorUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1：活动审批状态机关键边界（时间非法、审批意见、非审批人、撤销）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityApplyServiceImplTest {

    @Mock private ActivityApplyMapper activityApplyMapper;
    @Mock private ActivityApproveFlowMapper activityApproveFlowMapper;
    @Mock private ActivityApplyHistoryMapper activityApplyHistoryMapper;
    @Mock private ActivityCategoryMapper activityCategoryMapper;
    @Mock private ActivitySignConfigMapper activitySignConfigMapper;
    @Mock private SysClubMapper sysClubMapper;
    @Mock private SysCollegeMapper sysCollegeMapper;
    @Mock private SysDepartmentMapper sysDepartmentMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private ActivityCodeGeneratorUtil activityCodeGeneratorUtil;
    @Mock private NoticeAutoPublisher noticeAutoPublisher;
    @Mock private AuthorService authorService;

    @InjectMocks
    private ActivityApplyServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", activityApplyMapper);
        LoginUserDetails user = new LoginUserDetails(
                TestDataFactory.user(1L, "admin", 2, 1), authorService, Level.ADMIN, null, null);
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        if (securityUtils != null) {
            securityUtils.close();
        }
    }

    @Test
    @DisplayName("submit: endTime 早于 startTime")
    void submit_invalidTimeRange_throws() {
        ActivitySubmitDTO dto = new ActivitySubmitDTO();
        dto.setClubId(1L);
        dto.setActivityName("测");
        dto.setCategoryId(1L);
        dto.setActivityType(1);
        dto.setActivityLevel(1);
        dto.setStartTime(LocalDateTime.now().plusDays(2));
        dto.setEndTime(LocalDateTime.now().plusDays(1));
        dto.setLocation("场地");
        dto.setActivityContent("内容足够长用于测试提交校验逻辑");

        assertThatThrownBy(() -> service.submit(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_TIME_INVALID_CODE);
    }

    @Test
    @DisplayName("submit: 活动等级非法")
    void submit_invalidLevel_throws() {
        ActivitySubmitDTO dto = new ActivitySubmitDTO();
        dto.setClubId(1L);
        dto.setActivityName("测");
        dto.setCategoryId(1L);
        dto.setActivityType(1);
        dto.setActivityLevel(9);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusDays(2));
        dto.setLocation("场地");
        dto.setActivityContent("内容足够长用于测试提交校验逻辑");

        assertThatThrownBy(() -> service.submit(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_LEVEL_INVALID_CODE);
    }

    @Test
    @DisplayName("approve: 申请不存在")
    void approve_notFound_throws() {
        when(activityApplyMapper.selectById(999L)).thenReturn(null);
        ActivityApproveDTO dto = new ActivityApproveDTO();
        dto.setVersion(1);
        dto.setOpinion("同意");
        assertThatThrownBy(() -> service.approve(999L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_APPLY_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("approve: 非当前审批人")
    void approve_notCurrentApprover_throws() {
        ActivityApply apply = TestDataFactory.activity(1L, "WH20260718143000001", 10L,
                ActivityApplyConstants.STATUS_PENDING);
        apply.setVersion(0);
        apply.setCurrentApproveStep(1);
        when(activityApplyMapper.selectById(1L)).thenReturn(apply);

        SysClub club = TestDataFactory.club(10L, "测社团", "文化体育类", 1L);
        club.setStatus(ClubApplyConstants.CLUB_STATUS_NORMAL);
        when(sysClubMapper.selectById(10L)).thenReturn(club);

        ActivityApproveFlow flow = new ActivityApproveFlow();
        flow.setId(1L);
        flow.setActivityId(1L);
        flow.setFlowType(ActivityApplyConstants.FLOW_TYPE_NORMAL);
        flow.setStep(1);
        flow.setApproveUserId(999L);
        when(activityApproveFlowMapper.selectOne(any())).thenReturn(flow);

        ActivityApproveDTO dto = new ActivityApproveDTO();
        dto.setVersion(0);
        dto.setOpinion("同意");

        assertThatThrownBy(() -> service.approve(1L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_NOT_CURRENT_APPROVER_CODE);
    }

    @Test
    @DisplayName("reject: 意见为空")
    void reject_emptyOpinion_throws() {
        ActivityApply apply = TestDataFactory.activity(1L, "WH20260718143000001", 1L, 1);
        when(activityApplyMapper.selectById(1L)).thenReturn(apply);

        ActivityApproveDTO dto = new ActivityApproveDTO();
        dto.setVersion(apply.getVersion() != null ? apply.getVersion() : 1);
        dto.setOpinion("  ");

        assertThatThrownBy(() -> service.reject(1L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_OPINION_REQUIRED_CODE);
    }

    @Test
    @DisplayName("cancel: 非申请人")
    void cancel_notApplicant_throws() {
        ActivityApply apply = TestDataFactory.activity(1L, "WH20260718143000001", 1L,
                ActivityApplyConstants.STATUS_PENDING);
        apply.setApplyUserId(99L);
        apply.setVersion(0);
        when(activityApplyMapper.selectById(1L)).thenReturn(apply);

        ActivityCancelDTO dto = new ActivityCancelDTO();
        dto.setVersion(0);
        dto.setReason("不想办了");

        assertThatThrownBy(() -> service.cancel(1L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_NOT_APPLICANT_CODE);
    }

    @Test
    @DisplayName("cancel: 已驳回不可再撤销")
    void cancel_rejectedStatus_throws() {
        ActivityApply apply = TestDataFactory.activity(1L, "WH20260718143000001", 1L,
                ActivityApplyConstants.STATUS_REJECTED);
        apply.setApplyUserId(1L);
        apply.setVersion(0);
        when(activityApplyMapper.selectById(1L)).thenReturn(apply);

        ActivityCancelDTO dto = new ActivityCancelDTO();
        dto.setVersion(0);
        dto.setReason("不想办了");

        assertThatThrownBy(() -> service.cancel(1L, dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ACT_STATUS_INVALID_CODE);
    }

    @Test
    @DisplayName("cancel: 申请人正常撤销")
    void cancel_success() {
        ActivityApply apply = TestDataFactory.activity(1L, "WH20260718143000001", 1L,
                ActivityApplyConstants.STATUS_PENDING);
        apply.setApplyUserId(1L);
        apply.setVersion(0);
        when(activityApplyMapper.selectById(1L)).thenReturn(apply);
        when(activityApplyMapper.updateById(any(ActivityApply.class))).thenReturn(1);

        ActivityCancelDTO dto = new ActivityCancelDTO();
        dto.setVersion(0);
        dto.setReason("计划变更");

        service.cancel(1L, dto);

        verify(activityApplyMapper).updateById(any(ActivityApply.class));
        verify(noticeAutoPublisher).publishActivityCancelNotice(any(ActivityApply.class));
    }
}
