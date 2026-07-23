package untiy.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.EIException;
import untiy.exception.Level;
import untiy.exception.UserPermissionCode;
import untiy.service.AuthorService;
import untiy.service.SysRoleService;
import untiy.service.SysUserRoleService;
import untiy.service.SysUserService;
import untiy.testsupport.TestDataFactory;

import java.util.Collections;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * P0：用户删除四条权限规则。
 */
@ExtendWith(MockitoExtension.class)
class UserPermissionUtilsTest {

    @Mock private SysUserService sysUserService;
    @Mock private SysUserRoleService sysUserRoleService;
    @Mock private SysRoleService sysRoleService;
    @Mock private AuthorService authorService;

    @InjectMocks
    private UserPermissionUtils permissionUtils;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Long userId, String username, int level, Long clubId) {
        SysUser u = TestDataFactory.user(userId, username, 1, 1);
        LoginUserDetails details = new LoginUserDetails(u, authorService, level, clubId, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    @Test
    @DisplayName("规则1：不能删自己")
    void deleteSelf_forbidden() {
        loginAs(1L, "president", Level.CLUB_LEADER, 10L);
        assertThatThrownBy(() -> permissionUtils.checkDeletePermission(1L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(UserPermissionCode.SELF_DELETE_CODE);
    }

    @Test
    @DisplayName("规则2：非社长(level>2)禁止删除")
    void deleteByStudent_forbidden() {
        loginAs(1L, "student", Level.STUDENT, 10L);
        assertThatThrownBy(() -> permissionUtils.checkDeletePermission(2L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(UserPermissionCode.PERMISSION_DENIED_CODE);
    }

    @Test
    @DisplayName("规则3：同级/更高等级禁止删除")
    void deleteSameOrHigherLevel_forbidden() {
        loginAs(1L, "president", Level.CLUB_LEADER, 10L);

        SysUser target = TestDataFactory.student(2L, "other_president");
        when(sysUserService.getById(2L)).thenReturn(target);

        SysUserRole ur = TestDataFactory.userRole(1L, 2L, 5L, ClubApplyConstants.SCOPE_TYPE_CLUB, 10L);
        when(sysUserRoleService.list(any())).thenReturn(Collections.singletonList(ur));

        SysRole presidentRole = TestDataFactory.role(5L, "CLUB_PRESIDENT", Level.CLUB_LEADER, 2);
        when(sysRoleService.listByIds(anyCollection())).thenReturn(Collections.singletonList(presidentRole));

        assertThatThrownBy(() -> permissionUtils.checkDeletePermission(2L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(UserPermissionCode.LEVEL_INSUFFICIENT_CODE);
    }

    @Test
    @DisplayName("规则4：跨社团禁止删除")
    void deleteCrossClub_forbidden() {
        loginAs(1L, "president", Level.CLUB_LEADER, 10L);

        SysUser target = TestDataFactory.student(3L, "member_other_club");
        when(sysUserService.getById(3L)).thenReturn(target);

        SysUserRole ur = TestDataFactory.userRole(1L, 3L, 6L, ClubApplyConstants.SCOPE_TYPE_CLUB, 99L);
        when(sysUserRoleService.list(any())).thenReturn(Collections.singletonList(ur));

        SysRole memberRole = TestDataFactory.role(6L, "MEMBER", Level.STUDENT, 4);
        when(sysRoleService.listByIds(anyCollection())).thenReturn(Collections.singletonList(memberRole));

        assertThatThrownBy(() -> permissionUtils.checkDeletePermission(3L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(UserPermissionCode.SCOPE_MISMATCH_CODE);
    }

    @Test
    @DisplayName("合法：社长删同社团更低等级成员")
    void deleteMemberSameClub_ok() {
        loginAs(1L, "president", Level.CLUB_LEADER, 10L);

        SysUser target = TestDataFactory.student(3L, "member");
        when(sysUserService.getById(3L)).thenReturn(target);

        SysUserRole ur = TestDataFactory.userRole(1L, 3L, 6L, ClubApplyConstants.SCOPE_TYPE_CLUB, 10L);
        when(sysUserRoleService.list(any())).thenReturn(Collections.singletonList(ur));

        SysRole memberRole = TestDataFactory.role(6L, "MEMBER", Level.STUDENT, 4);
        when(sysRoleService.listByIds(anyCollection())).thenReturn(Collections.singletonList(memberRole));

        permissionUtils.checkDeletePermission(3L);
    }
}
