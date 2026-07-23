package untiy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import untiy.entity.ClubApplication;
import untiy.entity.ClubCategory;
import untiy.entity.SysCollege;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.ClubAdminApproveDTO;
import untiy.entity.dto.ClubCollegeApproveDTO;
import untiy.entity.dto.ClubCreateApplyDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.ClubApplicationMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ClubDissolveExecutor;
import untiy.security.LoginUserDetails;
import untiy.security.UserSecurityHelper;
import untiy.service.impl.ClubApplicationServiceImpl;
import untiy.testsupport.TestDataFactory;
import untiy.utils.ClubCodeGeneratorUtil;
import untiy.utils.SecurityUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1：社团创建申请 / 学院审批 / 校级审批边界。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClubApplicationServiceImplTest {

    @Mock private ClubApplicationMapper clubApplicationMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private SysClubMapper sysClubMapper;
    @Mock private SysCollegeMapper sysCollegeMapper;
    @Mock private ClubCodeGeneratorUtil clubCodeGeneratorUtil;
    @Mock private ClubDissolveExecutor clubDissolveExecutor;
    @Mock private AuthorService authorService;

    @InjectMocks
    private ClubApplicationServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", clubApplicationMapper);
        SysUser u = TestDataFactory.user(100L, "advisor1", 2, 1);
        LoginUserDetails advisorUser = new LoginUserDetails(u, authorService, Level.ADMIN, null, null);
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(advisorUser);

        SysRole advisorRole = TestDataFactory.advisorRole(8L);
        SysUserRole ur = TestDataFactory.userRole(1L, 100L, 8L, null, null);
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(ur));
        when(sysRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(advisorRole));
    }

    @AfterEach
    void tearDown() {
        if (securityUtils != null) {
            securityUtils.close();
        }
    }

    @Test
    @DisplayName("createApply: 类别非法")
    void createApply_invalidCategory_throws() {
        ClubCreateApplyDTO dto = new ClubCreateApplyDTO();
        dto.setClubName("新社团");
        dto.setCollegeId(1L);
        dto.setCategory("不存在的类");
        dto.setProposedLeaderUsername("leader1");
        dto.setMaxMembers(30);

        assertThatThrownBy(() -> service.createApply(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_CATEGORY_INVALID_CODE);
    }

    @Test
    @DisplayName("createApply: 同名社团重复")
    void createApply_duplicateName_throws() {
        ClubCreateApplyDTO dto = new ClubCreateApplyDTO();
        dto.setClubName("已有社团");
        dto.setCollegeId(1L);
        dto.setCategory(ClubCategory.CULTURE_SPORTS);
        dto.setProposedLeaderUsername("leader1");
        dto.setMaxMembers(30);

        SysUser leader = TestDataFactory.student(200L, "leader1");
        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.findInScope(eq(sysUserMapper), eq("leader1")))
                    .thenReturn(leader);
            helper.when(() -> UserSecurityHelper.assertUserEnabled(leader)).thenAnswer(inv -> null);
            when(clubDissolveExecutor.isClubNameDuplicateInCollege(1L, "已有社团")).thenReturn(true);

            assertThatThrownBy(() -> service.createApply(dto))
                    .isInstanceOf(EIException.class)
                    .extracting(e -> ((EIException) e).getCode())
                    .isEqualTo(ErrorConfig.CLUB_NAME_DUPLICATE_CODE);
        }
    }

    @Test
    @DisplayName("createApply: 正常提交")
    void createApply_success() {
        ClubCreateApplyDTO dto = new ClubCreateApplyDTO();
        dto.setClubName("新社团");
        dto.setCollegeId(1L);
        dto.setCategory(ClubCategory.CULTURE_SPORTS);
        dto.setProposedLeaderUsername("leader1");
        dto.setMaxMembers(30);

        SysUser leader = TestDataFactory.student(200L, "leader1");
        when(clubCodeGeneratorUtil.generateApplicationNo()).thenReturn("APP20260718001");
        when(clubApplicationMapper.insert(any(ClubApplication.class))).thenAnswer(inv -> {
            ClubApplication app = inv.getArgument(0);
            app.setId(1L);
            return 1;
        });

        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.findInScope(eq(sysUserMapper), eq("leader1")))
                    .thenReturn(leader);
            helper.when(() -> UserSecurityHelper.assertUserEnabled(leader)).thenAnswer(inv -> null);
            when(clubDissolveExecutor.isClubNameDuplicateInCollege(1L, "新社团")).thenReturn(false);

            String no = service.createApply(dto);
            assertThat(no).isEqualTo("APP20260718001");
            verify(clubApplicationMapper).insert(any(ClubApplication.class));
        }
    }

    @Test
    @DisplayName("approveCollege: 申请不存在")
    void approveCollege_notFound_throws() {
        when(clubApplicationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        ClubCollegeApproveDTO dto = new ClubCollegeApproveDTO();
        dto.setApplicationNo("NOPE");
        dto.setApproved(true);
        dto.setOpinion("同意");

        assertThatThrownBy(() -> service.approveCollege(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("approveCollege: 状态非法")
    void approveCollege_invalidStatus_throws() {
        ClubApplication app = new ClubApplication();
        app.setId(1L);
        app.setApplicationNo("APP1");
        app.setCollegeId(1L);
        app.setApplicantId(50L);
        app.setStatus(ClubApplyConstants.STATUS_COLLEGE_APPROVED);
        when(clubApplicationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(app);

        ClubCollegeApproveDTO dto = new ClubCollegeApproveDTO();
        dto.setApplicationNo("APP1");
        dto.setApproved(true);
        dto.setOpinion("同意");

        assertThatThrownBy(() -> service.approveCollege(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_APPLY_STATUS_INVALID_CODE);
    }

    @Test
    @DisplayName("approveCollege: 院长通过")
    void approveCollege_pass() {
        ClubApplication app = new ClubApplication();
        app.setId(1L);
        app.setApplicationNo("APP1");
        app.setCollegeId(1L);
        app.setApplicantId(50L);
        app.setStatus(ClubApplyConstants.STATUS_PENDING_COLLEGE);
        when(clubApplicationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(app);
        when(clubApplicationMapper.updateById(any(ClubApplication.class))).thenReturn(1);

        SysCollege college = TestDataFactory.college(1L, "计院");
        college.setDeanId(100L);
        when(sysCollegeMapper.selectById(1L)).thenReturn(college);

        ClubCollegeApproveDTO dto = new ClubCollegeApproveDTO();
        dto.setApplicationNo("APP1");
        dto.setApproved(true);
        dto.setOpinion("同意创建");

        service.approveCollege(dto);
        verify(clubApplicationMapper).updateById(ArgumentMatchers.argThat(a ->
                a.getStatus() != null && a.getStatus() == ClubApplyConstants.STATUS_COLLEGE_APPROVED));
    }

    @Test
    @DisplayName("approveAdmin: 无待校审申请")
    void approveAdmin_notFound_throws() {
        SysUser applicant = TestDataFactory.student(50L, "applicant1");
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(applicant);
        when(clubApplicationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ClubAdminApproveDTO dto = new ClubAdminApproveDTO();
        dto.setUsername("applicant1");
        dto.setApproved(true);
        dto.setOpinion("同意");

        assertThatThrownBy(() -> service.approveAdmin(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("approveAdmin: 校级驳回")
    void approveAdmin_reject() {
        SysUser applicant = TestDataFactory.student(50L, "applicant1");
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(applicant);

        ClubApplication app = new ClubApplication();
        app.setId(1L);
        app.setApplicantId(50L);
        app.setApplyType(ClubApplyConstants.APPLY_TYPE_CREATE);
        app.setStatus(ClubApplyConstants.STATUS_COLLEGE_APPROVED);
        when(clubApplicationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(app);
        when(clubApplicationMapper.updateById(any(ClubApplication.class))).thenReturn(1);

        // school admin: SUPER_ADMIN or ADMIN level role
        SysRole adminRole = TestDataFactory.role(9L, "SUPER_ADMIN", Level.SUPER_ADMIN, 0);
        SysUserRole ur = TestDataFactory.userRole(2L, 100L, 9L, null, null);
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(ur));
        when(sysRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(adminRole));

        ClubAdminApproveDTO dto = new ClubAdminApproveDTO();
        dto.setUsername("applicant1");
        dto.setApproved(false);
        dto.setOpinion("材料不足");

        service.approveAdmin(dto);
        verify(clubApplicationMapper).updateById(ArgumentMatchers.argThat(a ->
                a.getStatus() != null && a.getStatus() == ClubApplyConstants.STATUS_REJECTED));
    }
}
