package untiy.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import untiy.entity.SysUser;
import untiy.exception.EIException;
import untiy.exception.Level;
import untiy.exception.UserPermissionCode;
import untiy.mapper.SysUserMapper;
import untiy.security.LoginUserDetails;
import untiy.security.UserPermissionUtils;
import untiy.security.UserSecurityHelper;
import untiy.service.AuthorService;
import untiy.service.impl.SysUserServiceImpl;
import untiy.testsupport.TestDataFactory;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P0 残余：批量删除范围校验 + 自我更新权限边界。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysUserServiceImplUnitTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private UserPermissionUtils userPermissionUtils;
    @Mock private AuthorService authorService;

    @InjectMocks
    private SysUserServiceImpl service;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Long userId, String username, int level) {
        ReflectionTestUtils.setField(service, "baseMapper", sysUserMapper);
        SysUser u = TestDataFactory.user(userId, username, 1, 1);
        LoginUserDetails details = new LoginUserDetails(u, authorService, level, 10L, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    @Test
    @DisplayName("deleteUsers: 空列表直接返回")
    void deleteUsers_empty_noop() {
        ReflectionTestUtils.setField(service, "baseMapper", sysUserMapper);
        service.deleteUsers(Collections.emptyList());
        verify(sysUserMapper, never()).deleteByUsernames(any());
    }

    @Test
    @DisplayName("deleteUsers: 范围外用户拒绝")
    void deleteUsers_outOfScope_throws() {
        ReflectionTestUtils.setField(service, "baseMapper", sysUserMapper);
        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.findInScope(eq(sysUserMapper), eq("ghost")))
                    .thenReturn(null);
            assertThatThrownBy(() -> service.deleteUsers(Collections.singletonList("ghost")))
                    .isInstanceOf(AccessDeniedException.class);
            verify(sysUserMapper, never()).deleteByUsernames(any());
        }
    }

    @Test
    @DisplayName("deleteUsers: 范围内批量删除")
    void deleteUsers_inScope_ok() {
        ReflectionTestUtils.setField(service, "baseMapper", sysUserMapper);
        SysUser a = TestDataFactory.student(2L, "a");
        SysUser b = TestDataFactory.student(3L, "b");
        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.findInScope(eq(sysUserMapper), eq("a"))).thenReturn(a);
            helper.when(() -> UserSecurityHelper.findInScope(eq(sysUserMapper), eq("b"))).thenReturn(b);
            when(sysUserMapper.deleteByUsernames(any())).thenReturn(2);

            service.deleteUsers(Arrays.asList("a", "b"));
            verify(sysUserMapper).deleteByUsernames(Arrays.asList("a", "b"));
        }
    }

    @Test
    @DisplayName("deleteByUsername: 走删除权限校验后删除")
    void deleteByUsername_checksPermission() {
        loginAs(1L, "president", Level.CLUB_LEADER);
        SysUser target = TestDataFactory.student(2L, "member");
        try (MockedStatic<UserSecurityHelper> helper = mockStatic(UserSecurityHelper.class)) {
            helper.when(() -> UserSecurityHelper.findInScope(eq(sysUserMapper), eq("member")))
                    .thenReturn(target);
            when(sysUserMapper.deleteByUsername("member")).thenReturn(1);

            service.deleteByUsername("member");

            verify(userPermissionUtils).checkDeletePermission(any(LoginUserDetails.class), eq(2L));
            verify(sysUserMapper).deleteByUsername("member");
        }
    }

    @Test
    @DisplayName("updateUser: 禁止改他人资料")
    void updateUser_otherUser_throws() {
        loginAs(1L, "me", Level.STUDENT);
        SysUser patch = new SysUser();
        patch.setUsername("other");
        patch.setRealName("别人");

        assertThatThrownBy(() -> service.updateUser(patch))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(UserPermissionCode.PERMISSION_DENIED_CODE);
    }

    @Test
    @DisplayName("updateUser: username 为空")
    void updateUser_blankUsername_throws() {
        loginAs(1L, "me", Level.STUDENT);
        SysUser patch = new SysUser();
        patch.setUsername("  ");

        assertThatThrownBy(() -> service.updateUser(patch))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(UserPermissionCode.TARGET_NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("deleteByUsername: 空串直接返回")
    void deleteByUsername_blank_noop() {
        ReflectionTestUtils.setField(service, "baseMapper", sysUserMapper);
        assertThatCode(() -> service.deleteByUsername(""))
                .doesNotThrowAnyException();
        verify(sysUserMapper, never()).deleteByUsername(any());
    }
}
