package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.vo.PortalClubVO;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.service.SysClubService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 社团服务实现
 *
 * @author 玖
 * @since 2026-07-18
 */
@Slf4j
@Service
public class SysClubServiceImpl extends ServiceImpl<SysClubMapper, SysClub> implements SysClubService {

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Override
    public List<PortalClubVO> portalList(String category) {
        LambdaQueryWrapper<SysClub> wrapper = new LambdaQueryWrapper<SysClub>()
                .eq(SysClub::getStatus, 1)
                .orderByAsc(SysClub::getCategory)
                .orderByDesc(SysClub::getCreateTime);
        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(SysClub::getCategory, category);
        }

        List<SysClub> clubs = list(wrapper);
        return clubs.stream().map(club -> {
            PortalClubVO vo = new PortalClubVO();
            vo.setClubName(club.getClubName());
            vo.setCategory(club.getCategory());
            vo.setDescription(club.getDescription());
            vo.setLogo(club.getLogo());

            if (club.getCollegeId() != null) {
                SysCollege college = sysCollegeMapper.selectById(club.getCollegeId());
                if (college != null) {
                    vo.setCollegeName(college.getCollegeName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }
}