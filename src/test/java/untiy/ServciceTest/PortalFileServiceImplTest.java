package untiy.ServciceTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import untiy.entity.ActivityApply;
import untiy.entity.NoticeInfo;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.constants.NoticeConstants;
import untiy.exception.EIException;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.service.PortalFileService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 门户图片上传 Service 业务逻辑测试。
 */
@SpringBootTest
@Transactional
@Rollback
class PortalFileServiceImplTest {

    @Autowired
    private PortalFileService portalFileService;
    @Autowired
    private NoticeInfoMapper noticeInfoMapper;
    @Autowired
    private ActivityApplyMapper activityApplyMapper;
    @Autowired
    private SysClubMapper sysClubMapper;
    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    private NoticeInfo notice;
    private ActivityApply activity;
    private SysClub club;
    private SysCollege college;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        notice = new NoticeInfo();
        notice.setNoticeNo("WH20260718143000001");
        notice.setTitle("上传测试通知");
        notice.setContent("用于测试图片上传");
        notice.setPublisherId(1L);
        notice.setReceiverType(NoticeConstants.RECEIVER_ALL_STUDENTS);
        notice.setStatus(NoticeConstants.STATUS_PUBLISHED);
        notice.setPublishTime(now);
        notice.setCreateTime(now);
        noticeInfoMapper.insert(notice);

        college = new SysCollege();
        college.setCollegeName("上传测试学院");
        college.setCollegeCode("TEST_UPLOAD");
        college.setCreateTime(now);
        sysCollegeMapper.insert(college);

        club = new SysClub();
        club.setClubName("上传测试社团");
        club.setClubCode("TEST_UPLOAD_CLUB");
        club.setCategory("文化体育类");
        club.setCollegeId(college.getId());
        club.setStatus(1);
        club.setCreateTime(now);
        sysClubMapper.insert(club);

        activity = new ActivityApply();
        activity.setActivityNo("WH" + now.format(dtf) + "00001");
        activity.setClubId(club.getId());
        activity.setActivityName("上传测试活动");
        activity.setStartTime(now.plusDays(1));
        activity.setEndTime(now.plusDays(1).plusHours(2));
        activity.setLocation("测试场地");
        activity.setApplyUserId(1L);
        activity.setApplyUsername("测试用户");
        activity.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        activity.setActivityLevel(ActivityApplyConstants.LEVEL_SCHOOL);
        activity.setCreateTime(now);
        activityApplyMapper.insert(activity);
    }

    @AfterEach
    void tearDown() {
        if (activity != null && activity.getId() != null) activityApplyMapper.deleteById(activity.getId());
        if (club != null && club.getId() != null) sysClubMapper.deleteById(club.getId());
        if (college != null && college.getId() != null) sysCollegeMapper.deleteById(college.getId());
        if (notice != null && notice.getId() != null) noticeInfoMapper.deleteById(notice.getId());
    }

    // ==================== 通知封面上传 ====================

    @Test
    @DisplayName("uploadNoticeCover: 上传成功 → 更新 coverImage 并返回路径")
    void uploadNoticeCover_shouldUploadAndUpdateCoverImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "test-image-content".getBytes());
        String path = portalFileService.uploadNoticeCover(file, notice.getNoticeNo());
        assertThat(path).startsWith("/portal/notice/WH/");
        assertThat(path).endsWith(".png");
        NoticeInfo updated = noticeInfoMapper.selectById(notice.getId());
        assertThat(updated.getCoverImage()).isEqualTo(path);
    }

    @Test
    @DisplayName("uploadNoticeCover: noticeNo 不存在 → 抛 6401")
    void uploadNoticeCover_shouldThrowWhenNoticeNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "test".getBytes());
        assertThatThrownBy(() -> portalFileService.uploadNoticeCover(file, "XX99999999999999999"))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("uploadNoticeCover: noticeNo 为空 → 抛 BAD_REQUEST")
    void uploadNoticeCover_shouldThrowWhenNoticeNoBlank() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "test".getBytes());
        assertThatThrownBy(() -> portalFileService.uploadNoticeCover(file, ""))
                .isInstanceOf(EIException.class);
    }

    // ==================== 文件校验 ====================

    @Test
    @DisplayName("文件校验: 空文件 → 抛 BAD_REQUEST")
    void fileValidation_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> portalFileService.uploadNoticeCover(file, notice.getNoticeNo()))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("文件校验: 不支持的类型 → 抛 BAD_REQUEST")
    void fileValidation_shouldRejectUnsupportedType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "fake-pdf".getBytes());
        assertThatThrownBy(() -> portalFileService.uploadNoticeCover(file, notice.getNoticeNo()))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("文件校验: 超大文件(6MB) → 抛 BAD_REQUEST")
    void fileValidation_shouldRejectOversizedFile() {
        byte[] bigContent = new byte[6 * 1024 * 1024];
        Arrays.fill(bigContent, (byte) 0x00);
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.png", "image/png", bigContent);
        assertThatThrownBy(() -> portalFileService.uploadNoticeCover(file, notice.getNoticeNo()))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("文件校验: jpeg 扩展名归一化为 jpg")
    void fileValidation_shouldNormalizeJpegToJpg() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpeg", "image/jpeg", "test-jpeg".getBytes());
        String path = portalFileService.uploadNoticeCover(file, notice.getNoticeNo());
        assertThat(path).endsWith(".jpg");
    }

    // ==================== 活动封面上传 ====================

    @Test
    @DisplayName("uploadActivityCover: 上传成功 → 更新 coverImage")
    void uploadActivityCover_shouldUploadActivityCover() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "activity_cover.png", "image/png", "test-activity".getBytes());
        String path = portalFileService.uploadActivityCover(file, activity.getActivityNo());
        assertThat(path).startsWith("/portal/activity/WH/");
        ActivityApply updated = activityApplyMapper.selectById(activity.getId());
        assertThat(updated.getCoverImage()).isEqualTo(path);
    }

    @Test
    @DisplayName("uploadActivityCover: activityNo 不存在 → 抛 7301")
    void uploadActivityCover_shouldThrowWhenActivityNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "test".getBytes());
        assertThatThrownBy(() -> portalFileService.uploadActivityCover(file, "XX99999999999999999"))
                .isInstanceOf(EIException.class);
    }

    // ==================== 社团 Logo 上传 ====================

    @Test
    @DisplayName("uploadClubLogo: 上传成功 → 更新 logo")
    void uploadClubLogo_shouldUploadClubLogo() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", "test-logo".getBytes());
        String path = portalFileService.uploadClubLogo(file, club.getId());
        assertThat(path).startsWith("/portal/club/WH/");
        SysClub updated = sysClubMapper.selectById(club.getId());
        assertThat(updated.getLogo()).isEqualTo(path);
    }

    @Test
    @DisplayName("uploadClubLogo: clubId 不存在 → 抛 7202")
    void uploadClubLogo_shouldThrowWhenClubNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", "test".getBytes());
        assertThatThrownBy(() -> portalFileService.uploadClubLogo(file, 999999L))
                .isInstanceOf(EIException.class);
    }
}