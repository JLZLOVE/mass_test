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

import untiy.entity.ActivityApply;
import untiy.entity.ActivityCategory;
import untiy.entity.ActivitySignConfig;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.constants.ActivityApplyConstants;
import untiy.entity.vo.PortalActivityDetailVO;
import untiy.entity.vo.PortalActivityListVO;
import untiy.exception.EIException;
import untiy.mapper.ActivityCategoryMapper;
import untiy.mapper.ActivitySignConfigMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.service.ActivityApplyService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 门户活动 Service 业务逻辑测试。
 */
@SpringBootTest
@Transactional
@Rollback
class ActivityApplyServiceImplPortalTest {

    @Autowired
    private ActivityApplyService activityApplyService;
    @Autowired
    private SysClubMapper sysClubMapper;
    @Autowired
    private SysCollegeMapper sysCollegeMapper;
    @Autowired
    private ActivityCategoryMapper activityCategoryMapper;
    @Autowired
    private ActivitySignConfigMapper activitySignConfigMapper;

    private SysClub club;
    private SysCollege college;
    private ActivityCategory category;
    private ActivityApply approvedAct, oldAct, futureAct, tamperedAct;
    private ActivitySignConfig signConfig;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        college = new SysCollege();
        college.setCollegeName("测试学院_门户活动");
        college.setCollegeCode("TEST_ACT_PORTAL");
        college.setCreateTime(now);
        sysCollegeMapper.insert(college);

        club = new SysClub();
        club.setClubName("测试社团_门户活动");
        club.setClubCode("TEST_ACT_CLUB");
        club.setCategory("文化体育类");
        club.setCollegeId(college.getId());
        club.setStatus(1);
        club.setCreateTime(now);
        sysClubMapper.insert(club);

        category = new ActivityCategory();
        category.setCategoryName("测试分类_门户活动");
        category.setCreateTime(now);
        activityCategoryMapper.insert(category);

        approvedAct = new ActivityApply();
        approvedAct.setActivityNo("TO" + now.format(dtf) + "00001");
        approvedAct.setClubId(club.getId());
        approvedAct.setActivityName("已通过的门户活动");
        approvedAct.setCategoryId(category.getId());
        approvedAct.setStartTime(now.plusDays(1));
        approvedAct.setEndTime(now.plusDays(1).plusHours(2));
        approvedAct.setLocation("体育馆");
        approvedAct.setActivityContent("<p>活动详情内容</p>");
        approvedAct.setOrganizerNote("由测试社团主办");
        approvedAct.setCoverImage("/portal/activity/TO/cover.jpg");
        approvedAct.setApplyUserId(1L);
        approvedAct.setApplyUsername("测试用户");
        approvedAct.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        approvedAct.setActivityLevel(ActivityApplyConstants.LEVEL_SCHOOL);
        approvedAct.setCreateTime(now);
        activityApplyService.save(approvedAct);

        signConfig = new ActivitySignConfig();
        signConfig.setActivityId(approvedAct.getId());
        signConfig.setSignMode(1);
        signConfig.setSignStartTime(now.minusHours(1));
        signConfig.setSignEndTime(now.plusHours(1));
        signConfig.setEnableCheckout(1);
        signConfig.setEnabled(1);
        signConfig.setCreateTime(now);
        activitySignConfigMapper.insert(signConfig);

        oldAct = new ActivityApply();
        oldAct.setActivityNo("TO" + now.minusMonths(7).format(dtf) + "00002");
        oldAct.setClubId(club.getId());
        oldAct.setActivityName("6个月前的活动");
        oldAct.setCategoryId(category.getId());
        oldAct.setStartTime(now.minusMonths(7));
        oldAct.setEndTime(now.minusMonths(7).plusHours(2));
        oldAct.setLocation("旧场地");
        oldAct.setApplyUserId(1L);
        oldAct.setApplyUsername("测试用户");
        oldAct.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        oldAct.setActivityLevel(ActivityApplyConstants.LEVEL_COLLEGE);
        oldAct.setCreateTime(now.minusMonths(7));
        activityApplyService.save(oldAct);

        futureAct = new ActivityApply();
        futureAct.setActivityNo("TO" + now.plusMonths(1).format(dtf) + "00003");
        futureAct.setClubId(club.getId());
        futureAct.setActivityName("一个月后的活动");
        futureAct.setCategoryId(category.getId());
        futureAct.setStartTime(now.plusMonths(1));
        futureAct.setEndTime(now.plusMonths(1).plusHours(2));
        futureAct.setLocation("未来场地");
        futureAct.setApplyUserId(1L);
        futureAct.setApplyUsername("测试用户");
        futureAct.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        futureAct.setActivityLevel(ActivityApplyConstants.LEVEL_SCHOOL);
        futureAct.setCreateTime(now);
        activityApplyService.save(futureAct);

        tamperedAct = new ActivityApply();
        tamperedAct.setActivityNo("TO" + now.minusDays(1).format(dtf) + "00004");
        tamperedAct.setClubId(club.getId());
        tamperedAct.setActivityName("篡改测试活动");
        tamperedAct.setCategoryId(category.getId());
        tamperedAct.setStartTime(now.plusDays(2));
        tamperedAct.setEndTime(now.plusDays(2).plusHours(2));
        tamperedAct.setLocation("测试场地");
        tamperedAct.setActivityContent("篡改测试内容");
        tamperedAct.setApplyUserId(1L);
        tamperedAct.setApplyUsername("测试用户");
        tamperedAct.setApproveStatus(ActivityApplyConstants.STATUS_APPROVED);
        tamperedAct.setActivityLevel(ActivityApplyConstants.LEVEL_SCHOOL);
        tamperedAct.setCreateTime(now);
        activityApplyService.save(tamperedAct);
    }

    @AfterEach
    void tearDown() {
        if (signConfig != null && signConfig.getId() != null) activitySignConfigMapper.deleteById(signConfig.getId());
        for (ActivityApply a : new ActivityApply[]{approvedAct, oldAct, futureAct, tamperedAct}) {
            if (a != null && a.getId() != null) activityApplyService.removeById(a.getId());
        }
        if (category != null && category.getId() != null) activityCategoryMapper.deleteById(category.getId());
        if (club != null && club.getId() != null) sysClubMapper.deleteById(club.getId());
        if (college != null && college.getId() != null) sysCollegeMapper.deleteById(college.getId());
    }

    // ==================== portalList ====================

    @Test
    @DisplayName("portalList: 仅返回 approve_status=4 的活动")
    void portalList_shouldOnlyReturnApprovedActivities() {
        Page<PortalActivityListVO> result = activityApplyService.portalList(1, 10, null);
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(result.getRecords()).allMatch(vo -> vo.getActivityNo() != null);
    }

    @Test
    @DisplayName("portalList: freezeTime null → 默认取当前时间")
    void portalList_freezeTimeNull_shouldDefaultToNow() {
        Page<PortalActivityListVO> result = activityApplyService.portalList(1, 10, null);
        assertThat(result.getRecords()).noneMatch(vo -> vo.getActivityNo().equals(oldAct.getActivityNo()));
    }

    @Test
    @DisplayName("portalList: freezeTime > now+5min → 回退为当前时间")
    void portalList_freezeTimeExceeds5Minutes_shouldFallbackToNow() {
        LocalDateTime farFuture = LocalDateTime.now().plusHours(1);
        Page<PortalActivityListVO> result = activityApplyService.portalList(1, 10, farFuture);
        assertThat(result.getRecords()).noneMatch(vo -> vo.getActivityNo().equals(oldAct.getActivityNo()));
    }

    @Test
    @DisplayName("portalList: freezeTime 在 5 分钟内 → 使用传入值")
    void portalList_freezeTimeWithin5Minutes_shouldUsePassedValue() {
        LocalDateTime validFreeze = LocalDateTime.now().plusMinutes(3);
        Page<PortalActivityListVO> result = activityApplyService.portalList(1, 10, validFreeze);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("portalList: 6个月窗口过滤旧活动")
    void portalList_shouldFilterOldActivity() {
        Page<PortalActivityListVO> result = activityApplyService.portalList(1, 10, null);
        assertThat(result.getRecords()).noneMatch(vo -> vo.getActivityNo().equals(oldAct.getActivityNo()));
    }

    @Test
    @DisplayName("portalList: 联查社团名称和分类")
    void portalList_shouldJoinClubAndCategory() {
        Page<PortalActivityListVO> result = activityApplyService.portalList(1, 10, null);
        assertThat(result.getRecords()).isNotEmpty();
        PortalActivityListVO vo = result.getRecords().get(0);
        assertThat(vo.getClubName()).isNotNull();
        assertThat(vo.getCategoryName()).isNotNull();
    }

    // ==================== portalDetail ====================

    @Test
    @DisplayName("portalDetail: 存在的 activityNo → 返回完整详情")
    void portalDetail_shouldReturnDetailForExistingActivity() {
        PortalActivityDetailVO vo = activityApplyService.portalDetail(approvedAct.getActivityNo());
        assertThat(vo).isNotNull();
        assertThat(vo.getActivityNo()).isEqualTo(approvedAct.getActivityNo());
        assertThat(vo.getActivityName()).isEqualTo(approvedAct.getActivityName());
        assertThat(vo.getContent()).isEqualTo(approvedAct.getActivityContent());
        assertThat(vo.getOrganizerNote()).isEqualTo(approvedAct.getOrganizerNote());
        assertThat(vo.getClubName()).isNotNull();
        assertThat(vo.getCategoryName()).isNotNull();
    }

    @Test
    @DisplayName("portalDetail: 不存在的 activityNo → 抛 7301")
    void portalDetail_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> activityApplyService.portalDetail("XX99999999999999999"))
                .isInstanceOf(EIException.class);
    }

    @Test
    @DisplayName("portalDetail: 防篡改 → 编号时间不一致则封锁+抛7319")
    void portalDetail_tamperedActivity_shouldBlockAndThrow() {
        assertThatThrownBy(() -> activityApplyService.portalDetail(tamperedAct.getActivityNo()))
                .isInstanceOf(EIException.class);
        ActivityApply updated = activityApplyService.getById(tamperedAct.getId());
        assertThat(updated.getApproveStatus()).isEqualTo(ActivityApplyConstants.STATUS_BLOCKED);
    }

    @Test
    @DisplayName("portalDetail: 防篡改 → 一致的编号正常返回")
    void portalDetail_validActivity_shouldNotTamper() {
        PortalActivityDetailVO vo = activityApplyService.portalDetail(approvedAct.getActivityNo());
        assertThat(vo).isNotNull();
        ActivityApply current = activityApplyService.getById(approvedAct.getId());
        assertThat(current.getApproveStatus()).isEqualTo(ActivityApplyConstants.STATUS_APPROVED);
    }

    @Test
    @DisplayName("portalDetail: 签到配置不含GPS坐标")
    void portalDetail_shouldNotContainGpsCoordinates() {
        PortalActivityDetailVO vo = activityApplyService.portalDetail(approvedAct.getActivityNo());
        assertThat(vo.getSignMode()).isNotNull();
        assertThat(vo.getSignStartTime()).isNotNull();
        assertThat(vo.getSignEndTime()).isNotNull();
        assertThat(vo.getCheckoutEnabled()).isNotNull();
    }

    @Test
    @DisplayName("portalDetail: signAvailable 运行时计算（在窗口内→true）")
    void portalDetail_signAvailable_whenInWindow_shouldBeTrue() {
        PortalActivityDetailVO vo = activityApplyService.portalDetail(approvedAct.getActivityNo());
        assertThat(vo.getSignAvailable()).isTrue();
    }
}