package untiy.ServciceTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.vo.PortalClubVO;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.service.SysClubService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 门户社团列表 Service 业务逻辑测试。
 */
@SpringBootTest
@Transactional
@Rollback
class SysClubServiceImplPortalTest {

    @Autowired
    private SysClubService sysClubService;
    @Autowired
    private SysClubMapper sysClubMapper;
    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    private SysCollege college;
    private SysClub club1, club2, club3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        college = new SysCollege();
        college.setCollegeName("测试学院_社团门户");
        college.setCollegeCode("TEST_CLUB_PORTAL");
        college.setCreateTime(now);
        sysCollegeMapper.insert(college);

        club1 = new SysClub();
        club1.setClubName("文化体育类社团");
        club1.setClubCode("TEST_CLUB_WH");
        club1.setCategory("文化体育类");
        club1.setCollegeId(college.getId());
        club1.setDescription("文化体育类社团简介");
        club1.setLogo("/portal/club/WH/logo1.png");
        club1.setStatus(1);
        club1.setCreateTime(now);
        sysClubMapper.insert(club1);

        club2 = new SysClub();
        club2.setClubName("学术科技类社团");
        club2.setClubCode("TEST_CLUB_XS");
        club2.setCategory("学术科技类");
        club2.setCollegeId(college.getId());
        club2.setDescription("学术科技类社团简介");
        club2.setLogo("/portal/club/XS/logo2.png");
        club2.setStatus(1);
        club2.setCreateTime(now.minusHours(1));
        sysClubMapper.insert(club2);

        club3 = new SysClub();
        club3.setClubName("已解散社团");
        club3.setClubCode("TEST_CLUB_DISSOLVED");
        club3.setCategory("志愿公益类");
        club3.setCollegeId(college.getId());
        club3.setDescription("已解散社团简介");
        club3.setStatus(0);
        club3.setCreateTime(now.minusDays(1));
        sysClubMapper.insert(club3);
    }

    @AfterEach
    void tearDown() {
        for (SysClub c : new SysClub[]{club1, club2, club3}) {
            if (c != null && c.getId() != null) sysClubMapper.deleteById(c.getId());
        }
        if (college != null && college.getId() != null) sysCollegeMapper.deleteById(college.getId());
    }

    @Test
    @DisplayName("portalList: 仅返回 status=1 的社团")
    void portalList_shouldOnlyReturnActiveClubs() {
        List<PortalClubVO> result = sysClubService.portalList(null);
        assertThat(result).isNotEmpty();
        // 确认我们插入的社团在结果中，已解散的不在
        assertThat(result).anyMatch(vo -> "文化体育类社团".equals(vo.getClubName()));
        assertThat(result).anyMatch(vo -> "学术科技类社团".equals(vo.getClubName()));
        assertThat(result).noneMatch(vo -> "已解散社团".equals(vo.getClubName()));
    }

    @Test
    @DisplayName("portalList: 按 category 筛选")
    void portalList_shouldFilterByCategory() {
        List<PortalClubVO> result = sysClubService.portalList("学术科技类");
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(vo -> "学术科技类".equals(vo.getCategory()));
        assertThat(result).anyMatch(vo -> "学术科技类社团".equals(vo.getClubName()));
    }

    @Test
    @DisplayName("portalList: category 为空 → 返回全部")
    void portalList_shouldReturnAllWhenCategoryIsNull() {
        List<PortalClubVO> result = sysClubService.portalList(null);
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("portalList: 不存在的 category → 返回空列表")
    void portalList_shouldReturnEmptyForUnknownCategory() {
        List<PortalClubVO> result = sysClubService.portalList("不存在的分类");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("portalList: 联查学院名称")
    void portalList_shouldJoinCollegeName() {
        List<PortalClubVO> result = sysClubService.portalList(null);
        assertThat(result).isNotEmpty();
        // 至少有一个社团联查到了学院名称
        assertThat(result).anyMatch(vo -> vo.getCollegeName() != null && !vo.getCollegeName().isEmpty());
    }

    @Test
    @DisplayName("portalList: 返回字段完整（clubName/category/description/logo/collegeName）")
    void portalList_shouldContainAllFields() {
        List<PortalClubVO> result = sysClubService.portalList(null);
        assertThat(result).isNotEmpty();
        PortalClubVO vo = result.get(0);
        assertThat(vo.getClubName()).isNotNull();
        assertThat(vo.getCategory()).isNotNull();
        assertThat(vo.getDescription()).isNotNull();
        assertThat(vo.getLogo()).isNotNull();
        assertThat(vo.getCollegeName()).isNotNull();
    }
}