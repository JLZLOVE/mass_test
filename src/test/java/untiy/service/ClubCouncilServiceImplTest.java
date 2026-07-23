package untiy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import untiy.entity.ClubCouncil;
import untiy.entity.SysClub;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.CouncilInitiateDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.ClubCouncilMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ClubDissolveExecutor;
import untiy.security.LoginUserDetails;
import untiy.service.impl.ClubCouncilServiceImpl;
import untiy.testsupport.TestDataFactory;
import untiy.utils.SecurityUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1：合议解散发起 / 签字边界。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClubCouncilServiceImplTest {

    @Mock private ClubCouncilMapper clubCouncilMapper;
    @Mock private SysClubMapper sysClubMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private ClubDissolveExecutor clubDissolveExecutor;
    @Mock private AuthorService authorService;

    @InjectMocks
    private ClubCouncilServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", clubCouncilMapper);
        SysUser u = TestDataFactory.user(USER_ID, "super1", 2, 1);
        LoginUserDetails login = new LoginUserDetails(u, authorService, Level.SUPER_ADMIN, null, null);
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(login);
        stubSuperAdmin();
    }

    @AfterEach
    void tearDown() {
        if (securityUtils != null) {
            securityUtils.close();
        }
    }

    private void stubSuperAdmin() {
        SysRole role = TestDataFactory.superAdminRole(1L);
        SysUserRole ur = TestDataFactory.userRole(1L, USER_ID, 1L, null, null);
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(ur));
        when(sysRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(role));
    }

    @Test
    @DisplayName("initiate: 已有进行中合议")
    void initiate_inProgress_throws() {
        SysClub club = TestDataFactory.club(10L, "测社团", "文化体育类", 1L);
        club.setClubCode("WH001");
        when(sysClubMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(club);
        when(clubCouncilMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1);

        CouncilInitiateDTO dto = new CouncilInitiateDTO();
        dto.setClubCode("WH001");
        dto.setReason("长期不活动");

        assertThatThrownBy(() -> service.initiate(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_COUNCIL_IN_PROGRESS_CODE);
    }

    @Test
    @DisplayName("initiate: 存在进行中活动")
    void initiate_hasActiveActivity_throws() {
        SysClub club = TestDataFactory.club(10L, "测社团", "文化体育类", 1L);
        club.setClubCode("WH001");
        when(sysClubMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(club);
        when(clubCouncilMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(clubDissolveExecutor.hasActiveActivities(10L)).thenReturn(true);

        CouncilInitiateDTO dto = new CouncilInitiateDTO();
        dto.setClubCode("WH001");
        dto.setReason("长期不活动");

        assertThatThrownBy(() -> service.initiate(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_HAS_ACTIVE_ACTIVITY_CODE);
    }

    @Test
    @DisplayName("initiate: 正常发起")
    void initiate_success() {
        SysClub club = TestDataFactory.club(10L, "测社团", "文化体育类", 1L);
        club.setClubCode("WH001");
        when(sysClubMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(club);
        when(clubCouncilMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(clubDissolveExecutor.hasActiveActivities(10L)).thenReturn(false);
        when(clubCouncilMapper.insert(any(ClubCouncil.class))).thenReturn(1);

        CouncilInitiateDTO dto = new CouncilInitiateDTO();
        dto.setClubCode("WH001");
        dto.setReason("长期不活动");

        service.initiate(dto);
        verify(clubCouncilMapper).insert(any(ClubCouncil.class));
    }

    @Test
    @DisplayName("sign: 普通学生无权限")
    void sign_student_forbidden() {
        SysUser student = TestDataFactory.student(9L, "stu");
        LoginUserDetails login = new LoginUserDetails(student, authorService, Level.STUDENT, 10L, null);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(login);

        assertThatThrownBy(() -> service.sign(1L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ROLE_NO_PERMISSION_CODE);
    }

    @Test
    @DisplayName("sign: 合议不存在")
    void sign_notFound_throws() {
        when(clubCouncilMapper.selectById(99L)).thenReturn(null);
        assertThatThrownBy(() -> service.sign(99L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_COUNCIL_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("sign: 重复签字")
    void sign_duplicate_throws() {
        ClubCouncil council = new ClubCouncil();
        council.setId(1L);
        council.setClubId(10L);
        council.setCollegeId(1L);
        council.setStatus(ClubApplyConstants.COUNCIL_IN_PROGRESS);
        council.setSignatories("[{\"username\":\"super1\",\"userId\":1,\"level\":0}]");
        when(clubCouncilMapper.selectById(1L)).thenReturn(council);

        assertThatThrownBy(() -> service.sign(1L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.CLUB_ALREADY_SIGNED_CODE);
        verify(clubDissolveExecutor, never()).executeDissolve(any());
    }

    @Test
    @DisplayName("sign: 未达阈值仅记录签字")
    void sign_notPassed_onlyRecord() {
        ClubCouncil council = new ClubCouncil();
        council.setId(1L);
        council.setClubId(10L);
        council.setCollegeId(1L);
        council.setStatus(ClubApplyConstants.COUNCIL_IN_PROGRESS);
        council.setSignatories("[]");
        when(clubCouncilMapper.selectById(1L)).thenReturn(council);
        when(clubCouncilMapper.updateById(any(ClubCouncil.class))).thenReturn(1);

        service.sign(1L);

        verify(clubCouncilMapper).updateById(any(ClubCouncil.class));
        verify(clubDissolveExecutor, never()).executeDissolve(any());
    }
}
