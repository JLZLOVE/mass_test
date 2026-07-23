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
import org.springframework.test.util.ReflectionTestUtils;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.dto.AssignRoleDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.UserRoleScopeHelper;
import untiy.security.UserSecurityHelper;
import untiy.service.AuthorService;
import untiy.service.SysRoleService;
import untiy.service.impl.SysUserRoleServiceImpl;
import untiy.testsupport.TestDataFactory;
import untiy.utils.SecurityUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * P0：角色分配 / 撤销边界。
 */
@ExtendWith(MockitoExtension.class)
class SysUserRoleServiceImplTest {

    @Mock private SysRoleService sysRoleService;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysClubMapper sysClubMapper;
    @Mock private SysCollegeMapper sysCollegeMapper;
    @Mock private SysDepartmentMapper sysDepartmentMapper;
    @Mock private AuthorService authorService;

    @InjectMocks
    private SysUserRoleServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;
    private MockedStatic<UserSecurityHelper> userSecurityHelper;
    private MockedStatic<UserRoleScopeHelper> scopeHelper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", sysUserRoleMapper);

        SysUser adminUser = TestDataFactory.user(1L, "admin", 2, 1);
        LoginUserDetails admin = new LoginUserDetails(adminUser, authorService, Level.ADMIN, null, null);

        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentLevel).thenReturn(Level.ADMIN);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(admin);

        userSecurityHelper = mockStatic(UserSecurityHelper.class);
        scopeHelper = mockStatic(UserRoleScopeHelper.class);
    }

    @AfterEach
    void tearDown() {
        if (securityUtils != null) {
            securityUtils.close();
        }
        if (userSecurityHelper != null) {
            userSecurityHelper.close();
        }
        if (scopeHelper != null) {
            scopeHelper.close();
        }
    }

    @Test
    @DisplayName("assign: 参数缺失")
    void assign_blankUsername_throws() {
        AssignRoleDTO dto = new AssignRoleDTO();
        dto.setRoleId(1L);
        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.BAD_REQUEST_CODE);
    }

    @Test
    @DisplayName("assign: 角色不存在")
    void assign_roleNotFound_throws() {
        SysUser target = TestDataFactory.student(2L, "stu");
        userSecurityHelper.when(() -> UserSecurityHelper.findActiveInScopeByUsername(sysUserMapper, "stu"))
                .thenReturn(target);
        when(sysRoleService.getById(99L)).thenReturn(null);

        AssignRoleDTO dto = new AssignRoleDTO();
        dto.setUsername("stu");
        dto.setRoleId(99L);

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ROLE_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("assign: 正常分配社团角色")
    void assign_clubRole_success() {
        SysUser target = TestDataFactory.student(2L, "stu");
        userSecurityHelper.when(() -> UserSecurityHelper.findActiveInScopeByUsername(sysUserMapper, "stu"))
                .thenReturn(target);

        SysRole president = TestDataFactory.role(5L, "CLUB_PRESIDENT", Level.CLUB_LEADER, 2);
        when(sysRoleService.getById(5L)).thenReturn(president);

        scopeHelper.when(() -> UserRoleScopeHelper.validateScope(eq(2), eq(2), eq(10L),
                        any(), any(), any())).thenAnswer(inv -> null);
        scopeHelper.when(() -> UserRoleScopeHelper.assertNoDuplicateAssignment(
                        any(), eq(2L), eq(5L), eq(2), eq(10L))).thenAnswer(inv -> null);

        when(sysUserRoleMapper.insert(any(SysUserRole.class))).thenReturn(1);

        AssignRoleDTO dto = new AssignRoleDTO();
        dto.setUsername("stu");
        dto.setRoleId(5L);
        dto.setScopeType(2);
        dto.setScopeId(10L);

        service.assign(dto);
        verify(sysUserRoleMapper).insert(argThat(ur ->
                ur.getUserId().equals(2L) && ur.getRoleId().equals(5L)
                        && Integer.valueOf(2).equals(ur.getScopeType())
                        && Long.valueOf(10L).equals(ur.getScopeId())));
    }

    @Test
    @DisplayName("assign: data_scope 全部时 scope 须为空")
    void assign_allScope_invalidScope_throws() {
        SysUser target = TestDataFactory.student(2L, "stu");
        userSecurityHelper.when(() -> UserSecurityHelper.findActiveInScopeByUsername(sysUserMapper, "stu"))
                .thenReturn(target);

        SysRole adminRole = TestDataFactory.role(1L, "SUPER_ADMIN", Level.SUPER_ADMIN, 0);
        when(sysRoleService.getById(1L)).thenReturn(adminRole);

        // current ADMIN(1) assigning SUPER_ADMIN(0) → LevelBasedAccess.checkNewLevel throws
        AssignRoleDTO dto = new AssignRoleDTO();
        dto.setUsername("stu");
        dto.setRoleId(1L);

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("assign: 重复分配由 helper 拒绝")
    void assign_duplicate_throws() {
        SysUser target = TestDataFactory.student(2L, "stu");
        userSecurityHelper.when(() -> UserSecurityHelper.findActiveInScopeByUsername(sysUserMapper, "stu"))
                .thenReturn(target);

        SysRole member = TestDataFactory.role(6L, "MEMBER", Level.STUDENT, 4);
        when(sysRoleService.getById(6L)).thenReturn(member);

        scopeHelper.when(() -> UserRoleScopeHelper.validateScope(eq(4), isNull(), isNull(),
                        any(), any(), any())).thenAnswer(inv -> null);
        scopeHelper.when(() -> UserRoleScopeHelper.assertNoDuplicateAssignment(
                        any(), eq(2L), eq(6L), isNull(), isNull()))
                .thenThrow(new EIException(ErrorConfig.ROLE_ASSIGN_DUPLICATE_CODE, ErrorConfig.ROLE_ASSIGN_DUPLICATE_MSG));

        AssignRoleDTO dto = new AssignRoleDTO();
        dto.setUsername("stu");
        dto.setRoleId(6L);

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ROLE_ASSIGN_DUPLICATE_CODE);
    }

    @Test
    @DisplayName("revoke: id 不存在")
    void revoke_notFound_throws() {
        when(sysUserRoleMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.revoke(999L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.USER_ROLE_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("revoke: 正常撤销")
    void revoke_success() {
        SysUserRole association = TestDataFactory.userRole(8L, 2L, 6L, null, null);
        when(sysUserRoleMapper.selectById(8L)).thenReturn(association);

        SysRole member = TestDataFactory.role(6L, "MEMBER", Level.STUDENT, 4);
        when(sysRoleService.getById(6L)).thenReturn(member);
        when(sysUserRoleMapper.deleteById(8L)).thenReturn(1);

        service.revoke(8L);
        verify(sysUserRoleMapper).deleteById(8L);
    }

    @Test
    @DisplayName("revoke: 撤销自己持有的角色拒绝")
    void revoke_ownRole_throws() {
        SysUserRole association = TestDataFactory.userRole(8L, 1L, 2L, null, null);
        when(sysUserRoleMapper.selectById(8L)).thenReturn(association);

        SysRole adminRole = TestDataFactory.role(2L, "ADMIN", Level.ADMIN, 0);
        when(sysRoleService.getById(2L)).thenReturn(adminRole);

        // LoginUserDetails authorities come from AuthorService — stub ROLE_ADMIN
        when(authorService.getAuthoritiesByUserId(1L))
                .thenReturn(java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));

        SysUser adminUser = TestDataFactory.user(1L, "admin", 2, 1);
        LoginUserDetails admin = new LoginUserDetails(adminUser, authorService, Level.ADMIN, null, null);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(admin);
        securityUtils.when(SecurityUtils::getCurrentLevel).thenReturn(Level.ADMIN);

        assertThatThrownBy(() -> service.revoke(8L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.ROLE_REVOKE_SELF_CODE);
    }
}
