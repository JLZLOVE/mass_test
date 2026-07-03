package untiy.ServciceTest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.EIException;
import untiy.security.LoginUserDetails;
import untiy.service.AuthorService;
import untiy.service.SysUserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Rollback
class SysUserServiceImplTest {

    @Autowired
    private SysUserService sysUserService;

    @MockBean
    private AuthorService authorService;

    private static final String TEST_USERNAME = "test_2026001";
    private static final String TEST_PASSWORD = "123456";

    @BeforeEach
    void setUp() {
        // 清理可能残留的测试数据
        cleanupUser(TEST_USERNAME);
        for (int i = 1; i <= 3; i++) {
            cleanupUser(TEST_USERNAME + "_" + i);
        }

        // Mock AuthorService，避免真实数据库查询权限
        when(authorService.getAuthoritiesByUserId(anyLong()))
                .thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // 默认以超管身份登录（effectiveLevel=0，数据范围无限制）
        setAuthentication(TEST_USERNAME, 0L, 0, null, null);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void cleanupUser(String username) {
        SysUser existing = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();
        if (existing != null) {
            sysUserService.removeById(existing.getId());
        }
    }

    private void setAuthentication(String username, Long userId, int level, Long clubId, Long deptId) {
        SysUser sysUser = new SysUser();
        sysUser.setId(userId);
        sysUser.setUsername(username);
        sysUser.setStatus(1);

        LoginUserDetails loginUser = new LoginUserDetails(sysUser, authorService, level, clubId, deptId);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ==================== 注册 ====================
    @Test
    void testRegister_Success_WithUsername() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setRealName("测试用户");
        dto.setPassword(TEST_PASSWORD);
        dto.setGender(1);
        dto.setUserType(1);

        sysUserService.register(dto);

        SysUser saved = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, TEST_USERNAME)
                .one();
        assertThat(saved).isNotNull();
        assertThat(saved.getRealName()).isEqualTo("测试用户");
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getPassword()).isNotEqualTo(TEST_PASSWORD);
    }

    @Test
    void testRegister_Success_WithoutUsername() {
        RegisterDTO dto = new RegisterDTO();
        dto.setRealName("测试用户");
        dto.setPassword(TEST_PASSWORD);
        dto.setGender(1);
        dto.setUserType(1);

        sysUserService.register(dto);

        SysUser saved = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, "测试用户")
                .one();
        assertThat(saved).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("测试用户");
    }

    @Test
    void testRegister_RealNameEmpty_ThrowsException() {
        RegisterDTO dto = new RegisterDTO();
        dto.setRealName("");
        dto.setPassword(TEST_PASSWORD);

        assertThatThrownBy(() -> sysUserService.register(dto))
                .isInstanceOf(EIException.class)
                .hasMessageContaining("密码或姓名为空");
    }

    @Test
    void testRegister_PasswordEmpty_ThrowsException() {
        RegisterDTO dto = new RegisterDTO();
        dto.setRealName("测试用户");
        dto.setPassword("");

        assertThatThrownBy(() -> sysUserService.register(dto))
                .isInstanceOf(EIException.class)
                .hasMessageContaining("密码或姓名为空");
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setRealName("测试用户");
        dto.setPassword(TEST_PASSWORD);
        sysUserService.register(dto);

        RegisterDTO duplicate = new RegisterDTO();
        duplicate.setUsername(TEST_USERNAME);
        duplicate.setRealName("重复测试");
        duplicate.setPassword(TEST_PASSWORD);

        assertThatThrownBy(() -> sysUserService.register(duplicate))
                .isInstanceOf(EIException.class);
    }

    // ==================== 分页查询 ====================
    @Test
    void testPageQuery_Success() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("分页测试");
        user.setStatus(1);
        user.setUserType(1);
        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        Map<String, Object> param = new HashMap<>();
        param.put("page", 1);
        param.put("limit", 10);

        SysUser queryCond = new SysUser();
        queryCond.setRealName("分页测试");

        IPage<SysUserDTO> page = sysUserService.pageQuery(param, queryCond);
        assertThat(page).isNotNull();
        assertThat(page.getTotal()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testPageQuery_NoResult() {
        Map<String, Object> param = new HashMap<>();
        param.put("page", 1);
        param.put("limit", 10);

        SysUser queryCond = new SysUser();
        queryCond.setRealName("不存在的名字XYZ");

        IPage<SysUserDTO> page = sysUserService.pageQuery(param, queryCond);
        assertThat(page).isNotNull();
        assertThat(page.getTotal()).isEqualTo(0);
    }

    // ==================== 详情查询 ====================
    @Test
    void testGetDetail_Success() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("详情测试");
        user.setStatus(1);
        user.setUserType(1);

        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        SysUserDTO dto = sysUserService.getDetail(TEST_USERNAME);
        assertThat(dto).isNotNull();
        assertThat(dto.getRealName()).isEqualTo("详情测试");
    }

    @Test
    void testGetDetail_NotFound_ReturnsNull() {
        SysUserDTO dto = sysUserService.getDetail("not_exist_user_99999");
        assertThat(dto).isNull();
    }

    // ==================== 新增用户 ====================
    @Test
    void testSaveUser_WithPassword_Encoded() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("新增测试");
        user.setPassword(TEST_PASSWORD);
        user.setUserType(1);
        user.setStatus(1);

        sysUserService.saveUser(user);

        SysUser saved = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, TEST_USERNAME)
                .one();
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo(TEST_PASSWORD);
    }

    @Test
    void testSaveUser_WithoutPassword_SavedDirectly() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("无密码测试");
        user.setStatus(1);

        sysUserService.saveUser(user);

        SysUser saved = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, TEST_USERNAME)
                .one();
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).isNull();
    }

    // ==================== 批量更新 ====================
    @Test
    void testUpdateUsers_Success() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("原姓名");
        user.setStatus(1);
        user.setUserType(1);
        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setRealName("新姓名");
        update.setPhone("13800138000");

        sysUserService.updateUsers(Arrays.asList(update));

        SysUser updated = sysUserService.getById(user.getId());
        assertThat(updated.getRealName()).isEqualTo("新姓名");
        assertThat(updated.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void testUpdateUsers_WithPassword_Encoded() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("原姓名");
        user.setStatus(1);
        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(TEST_PASSWORD);

        sysUserService.updateUsers(Arrays.asList(update));

        SysUser updated = sysUserService.getById(user.getId());
        assertThat(updated.getPassword()).isNotEqualTo(TEST_PASSWORD);
    }

    // ==================== 批量删除 ====================
    @Test
    void testDeleteUsers_Success() {
        for (int i = 1; i <= 3; i++) {
            SysUser user = new SysUser();
            user.setUsername(TEST_USERNAME + "_" + i);
            user.setRealName("批量删除_" + i);
            user.setStatus(1);
            user.setPassword(TEST_PASSWORD);
            sysUserService.saveUser(user);
        }

        List<String> names = Arrays.asList(
                TEST_USERNAME + "_1",
                TEST_USERNAME + "_2",
                TEST_USERNAME + "_3"
        );
        sysUserService.deleteUsers(names);

        long count = sysUserService.lambdaQuery()
                .in(SysUser::getUsername, names)
                .count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    void testDeleteUsers_EmptyList_DoesNothing() {
        sysUserService.deleteUsers(null);
        sysUserService.deleteUsers(Arrays.asList());
        // 无异常即通过
    }

    // ==================== 按用户名删除 ====================
    @Test
    void testDeleteByUsername_Success() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("待删除");
        user.setStatus(1);
        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        sysUserService.deleteByUsername(TEST_USERNAME);

        SysUser deleted = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, TEST_USERNAME)
                .one();
        assertThat(deleted).isNull();
    }

    @Test
    void testDeleteByUsername_NullOrEmpty_DoesNothing() {
        sysUserService.deleteByUsername(null);
        sysUserService.deleteByUsername("");
        // 无异常即通过
    }

    @Test
    void testDeleteByUsername_NotInScope_ThrowsException() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("越权删除测试");
        user.setStatus(1);
        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        // 切换到普通用户（level=4，只能看自己），但目标不是自己
        setAuthentication("other_user", 999L, 4, null, null);

        assertThatThrownBy(() -> sysUserService.deleteByUsername(TEST_USERNAME))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== 更新自己 ====================
    @Test
    void testUpdateUser_Success() {
        SysUser user = new SysUser();
        user.setUsername(TEST_USERNAME);
        user.setRealName("原姓名");
        user.setGender(0);
        user.setPhone("13000000000");
        user.setEmail("old@test.com");
        user.setAvatar("old.jpg");
        user.setStatus(1);
        user.setUserType(1);
        user.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(user);

        // 使用与数据库一致的身份信息
        setAuthentication(TEST_USERNAME, user.getId(), 0, null, null);

        SysUser update = new SysUser();
        update.setUsername(TEST_USERNAME);
        update.setRealName("新姓名");
        update.setGender(2);
        update.setPhone("13800138000");
        update.setEmail("new@test.com");
        update.setAvatar("new.jpg");
        // 恶意传入非白名单字段
        update.setPassword("hacked_password");
        update.setStatus(0);
        update.setUserType(3);

        sysUserService.updateUser(update);

        SysUser updated = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, TEST_USERNAME)
                .one();
        assertThat(updated.getRealName()).isEqualTo("新姓名");
        assertThat(updated.getGender()).isEqualTo(2);
        assertThat(updated.getPhone()).isEqualTo("13800138000");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
        assertThat(updated.getAvatar()).isEqualTo("new.jpg");
        // 非白名单字段不应被修改
        assertThat(updated.getPassword()).isNotEqualTo("hacked_password");
        assertThat(updated.getStatus()).isEqualTo(1);
        assertThat(updated.getUserType()).isEqualTo(1);
    }

    @Test
    void testUpdateUser_NotLoggedIn_ThrowsException() {
        SecurityContextHolder.clearContext();

        SysUser update = new SysUser();
        update.setUsername(TEST_USERNAME);

        assertThatThrownBy(() -> sysUserService.updateUser(update))
                .isInstanceOf(EIException.class)
                .hasMessageContaining("未登录");
    }

    @Test
    void testUpdateUser_NullUsername_ThrowsException() {
        SysUser update = new SysUser();
        update.setUsername(null);

        assertThatThrownBy(() -> sysUserService.updateUser(update))
                .isInstanceOf(EIException.class);
    }

    @Test
    void testUpdateUser_EmptyUsername_ThrowsException() {
        SysUser update = new SysUser();
        update.setUsername("   ");

        assertThatThrownBy(() -> sysUserService.updateUser(update))
                .isInstanceOf(EIException.class);
    }

    @Test
    void testUpdateUser_UserNotFound_ThrowsException() {
        setAuthentication(TEST_USERNAME, 999L, 0, null, null);

        SysUser update = new SysUser();
        update.setUsername("not_exist_user_99999");

        assertThatThrownBy(() -> sysUserService.updateUser(update))
                .isInstanceOf(EIException.class);
    }

    @Test
    void testUpdateUser_UpdateOther_ThrowsException() {
        SysUser target = new SysUser();
        target.setUsername(TEST_USERNAME);
        target.setRealName("目标用户");
        target.setStatus(1);
        target.setPassword(TEST_PASSWORD);
        sysUserService.saveUser(target);

        // 当前登录用户是其他人
        setAuthentication("other_user", 888L, 0, null, null);

        SysUser update = new SysUser();
        update.setUsername(TEST_USERNAME);
        update.setRealName("恶意修改");

        assertThatThrownBy(() -> sysUserService.updateUser(update))
                .isInstanceOf(EIException.class)
                .hasMessageContaining("权限不足");
    }
}
