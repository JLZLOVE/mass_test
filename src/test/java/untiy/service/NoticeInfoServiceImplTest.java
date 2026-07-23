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
import untiy.entity.NoticeCategory;
import untiy.entity.NoticeInfo;
import untiy.entity.SysUser;
import untiy.entity.constants.NoticeConstants;
import untiy.entity.dto.NoticeSendDTO;
import untiy.entity.vo.NoticeDetailVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.NoticeCategoryMapper;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.NoticeReadRecordMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.NoticeScopeHelper;
import untiy.service.impl.NoticeInfoServiceImpl;
import untiy.testsupport.TestDataFactory;
import untiy.utils.ActivityCodeGeneratorUtil;
import untiy.utils.SecurityUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1：通知发送草稿 / 发布状态 / 撤回 / 阅读统计。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoticeInfoServiceImplTest {

    @Mock private NoticeInfoMapper noticeInfoMapper;
    @Mock private NoticeCategoryMapper noticeCategoryMapper;
    @Mock private NoticeReadRecordMapper noticeReadRecordMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private SysClubMapper sysClubMapper;
    @Mock private SysDepartmentMapper sysDepartmentMapper;
    @Mock private ActivityCodeGeneratorUtil activityCodeGeneratorUtil;
    @Mock private AuthorService authorService;

    @InjectMocks
    private NoticeInfoServiceImpl service;

    private MockedStatic<SecurityUtils> securityUtils;
    private static final Long PUBLISHER_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", noticeInfoMapper);
        SysUser u = TestDataFactory.user(PUBLISHER_ID, "admin1", 2, 1);
        LoginUserDetails login = new LoginUserDetails(u, authorService, Level.ADMIN, null, null);
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(login);
    }

    @AfterEach
    void tearDown() {
        if (securityUtils != null) {
            securityUtils.close();
        }
    }

    private NoticeCategory enabledCategory() {
        NoticeCategory c = new NoticeCategory();
        c.setId(1L);
        c.setCategoryName("通知");
        c.setStatus(1);
        return c;
    }

    @Test
    @DisplayName("send: 草稿保存")
    void send_draft_saves() {
        when(noticeCategoryMapper.selectById(1L)).thenReturn(enabledCategory());
        when(sysUserMapper.selectById(PUBLISHER_ID)).thenReturn(TestDataFactory.user(PUBLISHER_ID, "admin1", 2, 1));
        when(noticeInfoMapper.insert(any(NoticeInfo.class))).thenAnswer(inv -> {
            NoticeInfo n = inv.getArgument(0);
            n.setId(10L);
            return 1;
        });

        try (MockedStatic<NoticeScopeHelper> scope = mockStatic(NoticeScopeHelper.class)) {
            scope.when(() -> NoticeScopeHelper.resolvePublisherType(any(), any(), any(), eq(PUBLISHER_ID)))
                    .thenReturn("ADMIN");
            scope.when(() -> NoticeScopeHelper.assertCanSendToScope(
                    anyString(), anyLong(), any(), any(), any(), any(), anyInt(), anyString()))
                    .thenAnswer(inv -> null);

            NoticeSendDTO dto = new NoticeSendDTO();
            dto.setTitle("标题");
            dto.setContent("内容");
            dto.setCategoryId(1L);
            dto.setReceiverType(1);
            dto.setReceiverValues("ALL");
            dto.setNeedConfirm(false);
            dto.setLongTermVisible(true);
            dto.setDraft(true);

            Long id = service.send(dto);
            assertThat(id).isEqualTo(10L);
            verify(noticeInfoMapper).insert(any(NoticeInfo.class));
        }
    }

    @Test
    @DisplayName("publishNow: 非草稿状态拒绝")
    void publishNow_notDraft_throws() {
        NoticeInfo notice = TestDataFactory.notice("N1", "t", 1, NoticeConstants.STATUS_PUBLISHED);
        notice.setId(1L);
        notice.setPublisherId(PUBLISHER_ID);
        when(noticeInfoMapper.selectById(1L)).thenReturn(notice);

        assertThatThrownBy(() -> service.publishNow(1L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.NOTICE_STATUS_INVALID_CODE);
    }

    @Test
    @DisplayName("withdraw: 非发布状态拒绝")
    void withdraw_notPublished_throws() {
        NoticeInfo notice = TestDataFactory.notice("N1", "t", 1, NoticeConstants.STATUS_DRAFT);
        notice.setId(1L);
        notice.setPublisherId(PUBLISHER_ID);
        notice.setRevocable(1);
        when(noticeInfoMapper.selectById(1L)).thenReturn(notice);

        assertThatThrownBy(() -> service.withdraw(1L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.NOTICE_STATUS_INVALID_CODE);
    }

    @Test
    @DisplayName("withdraw: 不可撤回")
    void withdraw_notRevocable_throws() {
        NoticeInfo notice = TestDataFactory.notice("N1", "t", 1, NoticeConstants.STATUS_PUBLISHED);
        notice.setId(1L);
        notice.setPublisherId(PUBLISHER_ID);
        notice.setRevocable(0);
        when(noticeInfoMapper.selectById(1L)).thenReturn(notice);

        assertThatThrownBy(() -> service.withdraw(1L))
                .isInstanceOf(EIException.class)
                .extracting(e -> ((EIException) e).getCode())
                .isEqualTo(ErrorConfig.NOTICE_NOT_REVOCABLE_CODE);
    }

    @Test
    @DisplayName("withdraw: 发布人撤回成功")
    void withdraw_success() {
        NoticeInfo notice = TestDataFactory.notice("N1", "t", 1, NoticeConstants.STATUS_PUBLISHED);
        notice.setId(1L);
        notice.setPublisherId(PUBLISHER_ID);
        notice.setRevocable(1);
        when(noticeInfoMapper.selectById(1L)).thenReturn(notice);
        when(noticeInfoMapper.updateById(any(NoticeInfo.class))).thenReturn(1);

        service.withdraw(1L);
        verify(noticeInfoMapper).updateById(any(NoticeInfo.class));
    }

    @Test
    @DisplayName("readStats: 0人已读")
    void readStats_zeroReads() {
        NoticeInfo notice = TestDataFactory.notice("N1", "t", 1, NoticeConstants.STATUS_PUBLISHED);
        notice.setId(1L);
        notice.setPublisherId(PUBLISHER_ID);
        notice.setLongTermVisible(0);
        when(noticeInfoMapper.selectById(1L)).thenReturn(notice);
        when(noticeReadRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0);

        NoticeDetailVO vo = service.readStats(1L);
        assertThat(vo.getReadCount()).isZero();
        assertThat(vo.getConfirmCount()).isZero();
        assertThat(vo.getReceiverCount()).isZero();
    }
}
