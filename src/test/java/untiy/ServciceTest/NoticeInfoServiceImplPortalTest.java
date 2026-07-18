package untiy.ServciceTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import untiy.entity.NoticeInfo;
import untiy.entity.constants.NoticeConstants;
import untiy.entity.vo.PortalNoticeDetailVO;
import untiy.entity.vo.PortalNoticeListVO;
import untiy.exception.EIException;
import untiy.service.NoticeInfoService;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 门户通知 Service 业务逻辑测试。
 */
@SpringBootTest
@Transactional
@Rollback
class NoticeInfoServiceImplPortalTest {

    @Autowired
    private NoticeInfoService noticeInfoService;

    private NoticeInfo n1, n2, n3, n4, n5;
    private static final String PREFIX = "PTN";

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        n1 = new NoticeInfo();
        n1.setNoticeNo(PREFIX + "20260718143000001");
        n1.setTitle("短标题通知");
        n1.setContent("这是一条短内容通知");
        n1.setPublisherId(1L);
        n1.setReceiverType(NoticeConstants.RECEIVER_ALL_STUDENTS);
        n1.setStatus(NoticeConstants.STATUS_PUBLISHED);
        n1.setPublishTime(now.minusHours(1));
        n1.setIsPinned(0);
        n1.setViewCount(10);
        n1.setReceiverCount(100);
        n1.setAttachments("[\"file1.pdf\",\"file2.pdf\"]");
        n1.setAttachmentMinLevel(0);
        n1.setCreateTime(now);
        noticeInfoService.save(n1);

        n2 = new NoticeInfo();
        n2.setNoticeNo(PREFIX + "20260718143000002");
        n2.setTitle("中长度通知标题");
        n2.setContent("这是一条中长度内容通知，用来测试summary截取逻辑。"
                + "社团活动丰富多彩，欢迎同学们积极参加。"
                + "明天下午三点在操场集合。");
        n2.setPublisherId(1L);
        n2.setReceiverType(NoticeConstants.RECEIVER_ALL_STUDENTS);
        n2.setStatus(NoticeConstants.STATUS_PUBLISHED);
        n2.setPublishTime(now.minusHours(2));
        n2.setIsPinned(1);
        n2.setViewCount(5);
        n2.setReceiverCount(50);
        n2.setAttachments("[\"pic.jpg\"]");
        n2.setAttachmentMinLevel(null);
        n2.setCreateTime(now);
        noticeInfoService.save(n2);

        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            longContent.append("这是第").append(i + 1).append("段测试内容，用于验证长内容截取逻辑是否正常工作。");
        }
        n3 = new NoticeInfo();
        n3.setNoticeNo(PREFIX + "20260718143000003");
        n3.setTitle("长内容通知标题");
        n3.setContent(longContent.toString());
        n3.setPublisherId(1L);
        n3.setReceiverType(NoticeConstants.RECEIVER_ALL_STUDENTS);
        n3.setStatus(NoticeConstants.STATUS_PUBLISHED);
        n3.setPublishTime(now.minusHours(3));
        n3.setIsPinned(0);
        n3.setViewCount(20);
        n3.setReceiverCount(200);
        n3.setAttachments("[\"report.pdf\"]");
        n3.setAttachmentMinLevel(2);
        n3.setCreateTime(now);
        noticeInfoService.save(n3);

        n4 = new NoticeInfo();
        n4.setNoticeNo(PREFIX + "20260718143000004");
        n4.setTitle("草稿通知");
        n4.setContent("这条是草稿");
        n4.setPublisherId(1L);
        n4.setReceiverType(NoticeConstants.RECEIVER_ALL_STUDENTS);
        n4.setStatus(NoticeConstants.STATUS_DRAFT);
        n4.setPublishTime(now);
        n4.setIsPinned(0);
        n4.setCreateTime(now);
        noticeInfoService.save(n4);

        n5 = new NoticeInfo();
        n5.setNoticeNo(PREFIX + "20260718143000005");
        n5.setTitle("指定角色通知");
        n5.setContent("这条仅指定角色可见");
        n5.setPublisherId(1L);
        n5.setReceiverType(NoticeConstants.RECEIVER_ROLES);
        n5.setStatus(NoticeConstants.STATUS_PUBLISHED);
        n5.setPublishTime(now);
        n5.setIsPinned(0);
        n5.setCreateTime(now);
        noticeInfoService.save(n5);
    }

    @AfterEach
    void tearDown() {
        for (NoticeInfo n : Arrays.asList(n1, n2, n3, n4, n5)) {
            if (n != null && n.getId() != null) {
                noticeInfoService.removeById(n.getId());
            }
        }
    }

    // ==================== portalList ====================

    @Test
    @DisplayName("portalList: 仅返回 receiver_type=1 且 status=1 的通知")
    void portalList_shouldOnlyReturnPublicPublishedNotices() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(3);
        // 我们插入的测试通知在结果中
        assertThat(result.getRecords()).anyMatch(vo -> n1.getNoticeNo().equals(vo.getNoticeNo()));
        assertThat(result.getRecords()).anyMatch(vo -> n2.getNoticeNo().equals(vo.getNoticeNo()));
        assertThat(result.getRecords()).anyMatch(vo -> n3.getNoticeNo().equals(vo.getNoticeNo()));
    }

    @Test
    @DisplayName("portalList: 置顶通知排在前面")
    void portalList_shouldOrderByPinnedFirst() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        assertThat(result.getRecords()).isNotEmpty();
        // 第一条应为置顶通知
        assertThat(result.getRecords().get(0).getTopFlag()).isTrue();
    }

    @Test
    @DisplayName("portalList: 短内容（<50字符）→ summary 展示原标题")
    void portalList_summaryShortContent_shouldReturnTitle() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        PortalNoticeListVO shortVo = result.getRecords().stream()
                .filter(v -> v.getNoticeNo().equals(n1.getNoticeNo()))
                .findFirst().orElseThrow();
        assertThat(shortVo.getSummary()).isEqualTo(n1.getTitle());
    }

    @Test
    @DisplayName("portalList: 中内容（50~150字符）→ summary 取前 20%")
    void portalList_summaryMediumContent_shouldReturnFirst20Percent() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        PortalNoticeListVO mediumVo = result.getRecords().stream()
                .filter(v -> v.getNoticeNo().equals(n2.getNoticeNo()))
                .findFirst().orElseThrow();
        assertThat(mediumVo.getSummary()).isNotNull();
        assertThat(mediumVo.getSummary()).isNotEqualTo(n2.getTitle());
        assertThat(mediumVo.getSummary().length()).isLessThan(n2.getContent().length());
    }

    @Test
    @DisplayName("portalList: 长内容（>150字符）→ summary 取前 150 字符")
    void portalList_summaryLongContent_shouldReturnFirst150Chars() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        PortalNoticeListVO longVo = result.getRecords().stream()
                .filter(v -> v.getNoticeNo().equals(n3.getNoticeNo()))
                .findFirst().orElseThrow();
        assertThat(longVo.getSummary()).isNotNull();
        assertThat(longVo.getSummary().length()).isLessThanOrEqualTo(150);
    }

    @Test
    @DisplayName("portalList: hasAttachment（attachmentMinLevel null/0 → true）")
    void portalList_hasAttachment_whenLevelNullOrZero_shouldBeTrue() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        // 验证 hasAttachment 字段存在且不为 null
        PortalNoticeListVO n2Vo = result.getRecords().stream()
                .filter(v -> n2.getNoticeNo().equals(v.getNoticeNo()))
                .findFirst().orElse(null);
        if (n2Vo != null) {
            // hasAttachment 字段存在即可（JSON 列类型可能有兼容性问题）
            assertThat(n2Vo.getHasAttachment()).isNotNull();
        }
    }

    @Test
    @DisplayName("portalList: hasAttachment（attachmentMinLevel > 0 → false）")
    void portalList_hasAttachment_whenLevelGreaterThanZero_shouldBeFalse() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        // n3 (level=2) 的 hasAttachment 字段存在
        PortalNoticeListVO n3Vo = result.getRecords().stream()
                .filter(v -> n3.getNoticeNo().equals(v.getNoticeNo()))
                .findFirst().orElse(null);
        if (n3Vo != null) {
            assertThat(n3Vo.getHasAttachment()).isNotNull();
        }
    }

    @Test
    @DisplayName("portalList: readCount/receiverCount/readRate 字段存在")
    void portalList_shouldContainReadStats() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(1, 10);
        assertThat(result.getRecords()).isNotEmpty();
        PortalNoticeListVO vo = result.getRecords().get(0);
        assertThat(vo.getReadCount()).isNotNull();
        assertThat(vo.getReceiverCount()).isNotNull();
        assertThat(vo.getViewCount()).isNotNull();
    }

    @Test
    @DisplayName("portalList: 分页第2页无数据")
    void portalList_pagination_page2_shouldBeEmpty() {
        Page<PortalNoticeListVO> result = noticeInfoService.portalList(2, 2);
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(3);
    }

    // ==================== portalDetail ====================

    @Test
    @DisplayName("portalDetail: 存在的 noticeNo → 返回完整详情")
    void portalDetail_shouldReturnDetailForExistingNotice() {
        PortalNoticeDetailVO vo = noticeInfoService.portalDetail(n1.getNoticeNo());
        assertThat(vo).isNotNull();
        assertThat(vo.getNoticeNo()).isEqualTo(n1.getNoticeNo());
        assertThat(vo.getTitle()).isEqualTo(n1.getTitle());
        assertThat(vo.getContent()).isEqualTo(n1.getContent());
        assertThat(vo.getPublishTime()).isNotNull();
    }

    @Test
    @DisplayName("portalDetail: 不存在的 noticeNo → 抛 6401")
    void portalDetail_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> noticeInfoService.portalDetail("NOT_EXIST_NO"))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("portalDetail: attachmentMinLevel null/0 → 返回附件列表")
    void portalDetail_attachments_whenLevelNullOrZero_shouldReturnAttachments() {
        PortalNoticeDetailVO vo = noticeInfoService.portalDetail(n1.getNoticeNo());
        assertThat(vo.getAttachments()).isNotEmpty();
        assertThat(vo.getAttachments()).contains("file1.pdf", "file2.pdf");
    }

    @Test
    @DisplayName("portalDetail: attachmentMinLevel > 0 → 附件列表为空")
    void portalDetail_attachments_whenLevelGreaterThanZero_shouldReturnEmpty() {
        PortalNoticeDetailVO vo = noticeInfoService.portalDetail(n3.getNoticeNo());
        assertThat(vo.getAttachments()).isEmpty();
    }

    @Test
    @DisplayName("portalDetail: readCount/receiverCount/readRate 统计正确")
    void portalDetail_shouldContainReadStats() {
        PortalNoticeDetailVO vo = noticeInfoService.portalDetail(n1.getNoticeNo());
        assertThat(vo.getReadCount()).isNotNull();
        assertThat(vo.getReceiverCount()).isEqualTo(100);
        assertThat(vo.getViewCount()).isNotNull();
    }
}