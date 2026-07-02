package untiy.ServciceTest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import untiy.converter.SysUserConverter;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysUserMapper;
import untiy.security.DataScopeHelper;
import untiy.security.FieldMaskHelper;
import untiy.security.LoginUserDetails;
import untiy.service.impl.SysUserServiceImpl;
import untiy.utils.MPUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysUserConverter sysUserConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private LoginUserDetails mockViewer;

    @BeforeEach
    void setUp() {
        mockViewer = mock(LoginUserDetails.class);
    }

    // ==================== 辅助方法 ====================

    private SysUser createSysUser(String username, Long id) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setId(id);
        return user;
    }

    // ==================== 测试 register ====================

    @Test
    void register_shouldSuccess_whenUsernameNotExist() {
        try (MockedStatic<DataScopeHelper> ignored = mockStatic(DataScopeHelper.class)) {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("testUser");
            dto.setRealName("Real Name");
            dto.setPassword("plainPassword");
            dto.setGender(1);
            dto.setUserType(2);

            when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
            // 模拟用户名不存在：使用 any(QueryWrapper.class) 匹配实际传入的 LambdaQueryWrapper
            when(sysUserMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            sysUserService.register(dto);

            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(sysUserMapper).insert(captor.capture());
            SysUser saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("testUser");
            assertThat(saved.getPassword()).isEqualTo("encodedPassword");
            assertThat(saved.getGender()).isEqualTo(1);
            assertThat(saved.getUserType()).isEqualTo(2);
            assertThat(saved.getStatus()).isEqualTo(1);
        }
    }

    @Test
    void register_shouldUseRealNameAsUsername_whenUsernameNull() {
        try (MockedStatic<DataScopeHelper> ignored = mockStatic(DataScopeHelper.class)) {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername(null);
            dto.setRealName("RealNameOnly");
            dto.setPassword("pass");

            when(passwordEncoder.encode("pass")).thenReturn("encoded");
            // 不需要模拟 selectOne，因为 username 为 null 时不会执行重复性检查

            sysUserService.register(dto);

            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(sysUserMapper).insert(captor.capture());
            assertThat(captor.getValue().getUsername()).isEqualTo("RealNameOnly");
        }
    }

    @Test
    void register_shouldThrowException_whenUsernameExists() {
        try (MockedStatic<DataScopeHelper> ignored = mockStatic(DataScopeHelper.class)) {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("existing");
            dto.setRealName("Real");
            dto.setPassword("pass");

            SysUser existing = createSysUser("existing", 1L);
            when(sysUserMapper.selectOne(any(QueryWrapper.class))).thenReturn(existing);

            assertThatThrownBy(() -> sysUserService.register(dto))
                    .isInstanceOf(EIException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorConfig.RGEISTER_ADD_NEW_USER_CODE);
            verify(sysUserMapper, never()).insert(any());
        }
    }

    @Test
    void register_shouldThrowException_whenRealNameOrPasswordEmpty() {
        try (MockedStatic<DataScopeHelper> ignored = mockStatic(DataScopeHelper.class)) {
            RegisterDTO dto1 = new RegisterDTO();
            dto1.setUsername("user");
            dto1.setRealName("");
            dto1.setPassword("pass");

            RegisterDTO dto2 = new RegisterDTO();
            dto2.setUsername("user");
            dto2.setRealName("Real");
            dto2.setPassword("");

            assertThatThrownBy(() -> sysUserService.register(dto1))
                    .isInstanceOf(EIException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE);
            assertThatThrownBy(() -> sysUserService.register(dto2))
                    .isInstanceOf(EIException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE);
            verify(sysUserMapper, never()).insert(any());
        }
    }

    // ==================== 测试 pageQuery ====================

    @Test
    void pageQuery_shouldReturnMaskedDtoPage() {
        try (MockedStatic<MPUtil> mpUtilMock = mockStatic(MPUtil.class);
             MockedStatic<DataScopeHelper> dataScopeMock = mockStatic(DataScopeHelper.class);
             MockedStatic<FieldMaskHelper> fieldMaskMock = mockStatic(FieldMaskHelper.class)) {

            Map<String, Object> param = new HashMap<>();
            param.put("current", 1);
            param.put("size", 10);
            SysUser condition = new SysUser();

            Page<SysUser> mockPage = new Page<>(1, 10);
            mpUtilMock.when(() -> MPUtil.getPage(param)).thenReturn(mockPage);
            mpUtilMock.when(() -> MPUtil.sort(any(QueryWrapper.class), eq(param)))
                    .thenAnswer(inv -> inv.getArgument(0));
            mpUtilMock.when(() -> MPUtil.between(any(QueryWrapper.class), eq(param)))
                    .thenAnswer(inv -> inv.getArgument(0));
            mpUtilMock.when(() -> MPUtil.likeOrEq(any(QueryWrapper.class), eq(condition)))
                    .thenAnswer(inv -> inv.getArgument(0));

            dataScopeMock.when(() -> DataScopeHelper.applySysUserScope(any(QueryWrapper.class)))
                    .thenAnswer(inv -> null);
            dataScopeMock.when(DataScopeHelper::currentUser).thenReturn(mockViewer);

            List<SysUser> entities = Arrays.asList(
                    createSysUser("user1", 1L),
                    createSysUser("user2", 2L)
            );
            IPage<SysUser> entityPage = new Page<>(1, 10, 2);
            entityPage.setRecords(entities);
            when(sysUserMapper.selectPage(eq(mockPage), any(QueryWrapper.class))).thenReturn(entityPage);

            SysUserDTO dto1 = new SysUserDTO();
            dto1.setUsername("user1");
            SysUserDTO dto2 = new SysUserDTO();
            dto2.setUsername("user2");
            when(sysUserConverter.toDto(any(SysUser.class))).thenReturn(dto1, dto2);

            IPage<SysUserDTO> result = sysUserService.pageQuery(param, condition);

            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getRecords().get(0).getUsername()).isEqualTo("user1");
            fieldMaskMock.verify(() -> FieldMaskHelper.maskSysUserDto(any(SysUserDTO.class), eq(mockViewer)), times(2));
        }
    }

    // ==================== 测试 getDetail ====================

    @Test
    void getDetail_shouldReturnMaskedDto_whenUserExists() {
        try (MockedStatic<DataScopeHelper> dataScopeMock = mockStatic(DataScopeHelper.class);
             MockedStatic<FieldMaskHelper> fieldMaskMock = mockStatic(FieldMaskHelper.class)) {

            String username = "testUser";
            SysUser entity = createSysUser(username, 1L);
            when(sysUserConverter.selectByUsername(username)).thenReturn(entity);

            SysUserDTO dto = new SysUserDTO();
            dto.setUsername(username);
            when(sysUserConverter.toDto(entity)).thenReturn(dto);

            dataScopeMock.when(DataScopeHelper::currentUser).thenReturn(mockViewer);

            SysUserDTO result = sysUserService.getDetail(username);
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(username);

            fieldMaskMock.verify(() -> FieldMaskHelper.maskSysUserDto(dto, mockViewer), times(1));
        }
    }

    @Test
    void getDetail_shouldReturnNull_whenUserNotExist() {
        when(sysUserConverter.selectByUsername(anyString())).thenReturn(null);
        assertThat(sysUserService.getDetail("unknown")).isNull();
    }

    // ==================== 测试 saveUser ====================

    @Test
    void saveUser_shouldEncodePassword_whenPasswordNotNull() {
        SysUser user = new SysUser();
        user.setUsername("newUser");
        user.setPassword("plain");
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        sysUserService.saveUser(user);

        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(sysUserMapper).insert(user);
    }

    @Test
    void saveUser_shouldNotEncode_whenPasswordNull() {
        SysUser user = new SysUser();
        user.setUsername("newUser");
        user.setPassword(null);

        sysUserService.saveUser(user);

        verify(passwordEncoder, never()).encode(any());
        verify(sysUserMapper).insert(user);
    }

    // ==================== 测试 updateUsers ====================

    @Test
    void updateUsers_shouldEncodeAndUpdateAll_whenInScope() {
        try (MockedStatic<DataScopeHelper> dataScopeMock = mockStatic(DataScopeHelper.class)) {
            SysUser user1 = createSysUser("user1", 1L);
            user1.setPassword("pass1");
            SysUser user2 = createSysUser("user2", 2L);
            user2.setPassword("pass2");
            List<SysUser> users = Arrays.asList(user1, user2);

            // 模拟权限检查：每次 findInScope 都会调用 selectOne，返回对应的用户
            when(sysUserMapper.selectOne(any(QueryWrapper.class)))
                    .thenReturn(user1, user2);

            when(passwordEncoder.encode("pass1")).thenReturn("encoded1");
            when(passwordEncoder.encode("pass2")).thenReturn("encoded2");

            // 关键：模拟 updateBatchById 避免 MyBatis-Plus 初始化异常
            when(sysUserMapper.updateBatchById(anyList())).thenReturn(true);

            sysUserService.updateUsers(users);

            assertThat(user1.getPassword()).isEqualTo("encoded1");
            assertThat(user2.getPassword()).isEqualTo("encoded2");

            // 验证 updateBatchById 被调用一次
            verify(sysUserMapper, times(1)).updateBatchById(users);
        }
    }

    @Test
    void updateUsers_shouldThrowException_whenUserNotInScope() {
        try (MockedStatic<DataScopeHelper> dataScopeMock = mockStatic(DataScopeHelper.class)) {
            SysUser user1 = createSysUser("user1", 1L);
            SysUser user2 = createSysUser("user2", 2L);
            when(sysUserMapper.selectOne(any(QueryWrapper.class)))
                    .thenReturn(user1)   // 第一个存在
                    .thenReturn(null);   // 第二个不存在

            assertThatThrownBy(() -> sysUserService.updateUsers(Arrays.asList(user1, user2)))
                    .isInstanceOf(AccessDeniedException.class);
            verify(sysUserMapper, never()).updateBatchById(anyList());
        }
    }

    // ==================== 测试 deleteUsers ====================

    @Test
    void deleteUsers_shouldDeleteAll_whenAllInScope() {
        try (MockedStatic<DataScopeHelper> dataScopeMock = mockStatic(DataScopeHelper.class)) {
            List<String> names = Arrays.asList("user1", "user2");
            when(sysUserMapper.selectOne(any(QueryWrapper.class)))
                    .thenReturn(createSysUser("user1", 1L))
                    .thenReturn(createSysUser("user2", 2L));

            sysUserService.deleteUsers(names);

            verify(sysUserMapper).deleteBatchIds(names);
        }
    }

    @Test
    void deleteUsers_shouldThrowException_whenOneNotInScope() {
        try (MockedStatic<DataScopeHelper> dataScopeMock = mockStatic(DataScopeHelper.class)) {
            List<String> names = Arrays.asList("user1", "user2");
            when(sysUserMapper.selectOne(any(QueryWrapper.class)))
                    .thenReturn(createSysUser("user1", 1L))
                    .thenReturn(null);

            assertThatThrownBy(() -> sysUserService.deleteUsers(names))
                    .isInstanceOf(AccessDeniedException.class);
            verify(sysUserMapper, never()).deleteBatchIds(any());
        }
    }

    @Test
    void deleteUsers_shouldDoNothing_whenNamesEmpty() {
        sysUserService.deleteUsers(null);
        sysUserService.deleteUsers(List.of());
        verify(sysUserMapper, never()).deleteBatchIds(any());
    }

    // ==================== 测试 deleteByUsername ====================

    @Test
    void deleteByUsername_shouldCallMapperDelete() {
        String username = "testUser";
        sysUserService.deleteByUsername(username);
        verify(sysUserMapper).deleteByUsername(username);
    }

    @Test
    void deleteByUsername_shouldDoNothing_whenUsernameNull() {
        sysUserService.deleteByUsername(null);
        verify(sysUserMapper, never()).deleteByUsername(any());
    }
}