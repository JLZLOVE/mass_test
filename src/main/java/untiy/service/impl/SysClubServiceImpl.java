package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import untiy.entity.ClubApplication;
import untiy.entity.ClubCategory;
import untiy.entity.ClubCouncil;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysDepartment;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.vo.ClubMemberCountVO;
import untiy.entity.vo.PortalClubVO;
import untiy.entity.vo.SysClubListVO;
import untiy.entity.vo.SysUserRoleVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.ClubApplicationMapper;
import untiy.mapper.ClubCouncilMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ClubLookupHelper;
import untiy.security.ClubSecurityHelper;
import untiy.security.LoginUserDetails;
import untiy.security.UserScopeResolver;
import untiy.service.SysClubService;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    public static final String TAB_NORMAL = "normal";
    public static final String TAB_DISSOLVING = "dissolving";
    public static final String TAB_COUNCIL = "council";

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysDepartmentMapper sysDepartmentMapper;

    @Autowired
    private ClubApplicationMapper clubApplicationMapper;

    @Autowired
    private ClubCouncilMapper clubCouncilMapper;

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

    @Override
    public IPage<SysClubListVO> adminPageQuery(Map<String, Object> param, SysClub query,
                                               String keyword, String tabMode) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (user.getEffectiveLevel() > Level.DEPT_LEADER) {
            throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, ErrorConfig.ROLE_NO_PERMISSION_MSG);
        }

        String tab = StringUtils.isBlank(tabMode) ? TAB_NORMAL : tabMode.trim().toLowerCase();
        Page<SysClub> page = MPUtil.getPage(param);
        LambdaQueryWrapper<SysClub> wrapper = new LambdaQueryWrapper<>();

        applyDataScope(wrapper, user);

        if (query != null) {
            if (StringUtils.isNotBlank(query.getCategory())) {
                wrapper.eq(SysClub::getCategory, query.getCategory());
            }
            if (query.getCollegeId() != null) {
                wrapper.eq(SysClub::getCollegeId, query.getCollegeId());
            }
            if (query.getStatus() != null) {
                wrapper.eq(SysClub::getStatus, query.getStatus());
            }
        }

        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysClub::getClubName, kw).or().like(SysClub::getClubCode, kw));
        }

        Map<Long, String> dissolvingAppNo = loadDissolvingAppNoMap();
        Set<Long> dissolvingIds = dissolvingAppNo.keySet();
        Map<Long, Long> councilIdByClub = loadActiveCouncilMap();
        Set<Long> councilClubIds = councilIdByClub.keySet();

        if (TAB_DISSOLVING.equals(tab)) {
            if (dissolvingIds.isEmpty()) {
                return emptyVoPage(page);
            }
            wrapper.in(SysClub::getId, dissolvingIds);
        } else if (TAB_COUNCIL.equals(tab)) {
            if (councilClubIds.isEmpty()) {
                return emptyVoPage(page);
            }
            wrapper.in(SysClub::getId, councilClubIds);
        } else {
            // 正常 Tab：排除进行中的解散申请与合议
            if (!dissolvingIds.isEmpty()) {
                wrapper.notIn(SysClub::getId, dissolvingIds);
            }
            if (!councilClubIds.isEmpty()) {
                wrapper.notIn(SysClub::getId, councilClubIds);
            }
            if (query == null || query.getStatus() == null) {
                wrapper.eq(SysClub::getStatus, ClubApplyConstants.CLUB_STATUS_NORMAL);
            }
        }

        wrapper.orderByDesc(SysClub::getCreateTime);
        IPage<SysClub> clubPage = page(page, wrapper);

        List<SysClubListVO> records = clubPage.getRecords().stream()
                .map(club -> toListVO(club, user, dissolvingAppNo, councilIdByClub))
                .collect(Collectors.toList());

        Page<SysClubListVO> result = new Page<>(clubPage.getCurrent(), clubPage.getSize(), clubPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public SysClubListVO getDetailByClubCode(String clubCode) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (user.getEffectiveLevel() > Level.DEPT_LEADER) {
            throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, ErrorConfig.ROLE_NO_PERMISSION_MSG);
        }
        SysClub club = ClubLookupHelper.findByClubCode(baseMapper, clubCode);
        if (club == null) {
            throw new EIException(ErrorConfig.CLUB_NOT_FOUND_CODE, ErrorConfig.CLUB_NOT_FOUND_MSG);
        }
        assertCanViewClub(user, club);
        return toListVO(club, user, loadDissolvingAppNoMap(), loadActiveCouncilMap());
    }

    @Override
    public List<SysDepartment> listDepartments(String clubCode) {
        SysClub club = requireViewableClub(clubCode);
        return sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getClubId, club.getId())
                .eq(SysDepartment::getStatus, 1)
                .orderByAsc(SysDepartment::getId));
    }

    @Override
    public IPage<SysUserRoleVO> pageMembers(Map<String, Object> param, String clubCode, String roleCode) {
        SysClub club = requireViewableClub(clubCode);
        Page<SysUserRoleVO> page = MPUtil.getPage(param);
        return sysUserRoleMapper.selectPageByClubScope(page, club.getId(),
                StringUtils.isBlank(roleCode) ? null : roleCode.trim());
    }

    @Override
    public List<ClubMemberCountVO> batchMemberCount(List<Long> clubIds) {
        if (clubIds == null || clubIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> distinct = clubIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClubMemberCountVO> counted = sysUserRoleMapper.countMembersByClubIds(distinct);
        Map<Long, Integer> map = counted.stream()
                .collect(Collectors.toMap(ClubMemberCountVO::getClubId, ClubMemberCountVO::getMemberCount, (a, b) -> a));
        List<ClubMemberCountVO> result = new ArrayList<>();
        for (Long id : distinct) {
            result.add(new ClubMemberCountVO(id, map.getOrDefault(id, 0)));
        }
        return result;
    }

    private SysClub requireViewableClub(String clubCode) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (user.getEffectiveLevel() > Level.DEPT_LEADER) {
            throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, ErrorConfig.ROLE_NO_PERMISSION_MSG);
        }
        SysClub club = ClubLookupHelper.findByClubCode(baseMapper, clubCode);
        if (club == null) {
            throw new EIException(ErrorConfig.CLUB_NOT_FOUND_CODE, ErrorConfig.CLUB_NOT_FOUND_MSG);
        }
        assertCanViewClub(user, club);
        return club;
    }

    private void assertCanViewClub(LoginUserDetails user, SysClub club) {
        int level = user.getEffectiveLevel();
        if (level <= Level.ADMIN) {
            return;
        }
        if (level == Level.CLUB_LEADER) {
            Set<Long> clubIds = resolvePresidentClubIds(user.getUserId());
            if (!clubIds.contains(club.getId())) {
                throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, "您无权查看该社团详情");
            }
            return;
        }
        if (level == Level.DEPT_LEADER) {
            Long deptId = user.getPrimaryDepartmentId();
            if (deptId == null) {
                throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, "您无权查看该社团详情");
            }
            SysDepartment dept = sysDepartmentMapper.selectById(deptId);
            if (dept == null || !Objects.equals(dept.getClubId(), club.getId())) {
                throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, "您无权查看该社团详情");
            }
        }
    }

    private void applyDataScope(LambdaQueryWrapper<SysClub> wrapper, LoginUserDetails user) {
        int level = user.getEffectiveLevel();
        if (level <= Level.ADMIN) {
            // 指导老师仅看自己指导的社团；校级/院级管理员看全量（学院范围由 collegeId 筛选项约束）
            if (ClubSecurityHelper.hasAdvisorRole(sysUserRoleMapper, sysRoleMapper, user.getUserId())
                    && !isSchoolAdmin(user.getUserId())) {
                wrapper.eq(SysClub::getAdvisorId, user.getUserId());
            }
            return;
        }
        if (level == Level.CLUB_LEADER) {
            Set<Long> clubIds = resolvePresidentClubIds(user.getUserId());
            if (clubIds.isEmpty()) {
                wrapper.eq(SysClub::getId, -1L);
            } else {
                wrapper.in(SysClub::getId, clubIds);
            }
            return;
        }
        if (level == Level.DEPT_LEADER) {
            Long deptId = user.getPrimaryDepartmentId();
            if (deptId == null) {
                wrapper.eq(SysClub::getId, -1L);
                return;
            }
            SysDepartment dept = sysDepartmentMapper.selectById(deptId);
            if (dept == null || dept.getClubId() == null) {
                wrapper.eq(SysClub::getId, -1L);
            } else {
                wrapper.eq(SysClub::getId, dept.getClubId());
            }
        }
    }

    private boolean isSchoolAdmin(Long userId) {
        List<SysRole> roles = UserScopeResolver.loadActiveRoles(sysUserRoleMapper, sysRoleMapper, userId);
        int level = UserScopeResolver.resolveEffectiveLevel(roles);
        if (level == Level.SUPER_ADMIN) {
            return true;
        }
        return level == Level.ADMIN && roles.stream().anyMatch(r -> !UserScopeResolver.isAdvisorRoleCode(r.getRoleCode()));
    }

    private Set<Long> resolvePresidentClubIds(Long userId) {
        List<SysRole> roles = UserScopeResolver.loadActiveRoles(sysUserRoleMapper, sysRoleMapper, userId);
        Set<Long> presidentRoleIds = roles.stream()
                .filter(r -> ClubApplyConstants.ROLE_CLUB_PRESIDENT.equals(r.getRoleCode()))
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        if (presidentRoleIds.isEmpty()) {
            return Collections.emptySet();
        }
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_CLUB)
                        .in(SysUserRole::getRoleId, presidentRoleIds))
                .stream()
                .map(SysUserRole::getScopeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 进行中的解散申请：apply_type=2 且 status∈{1,2}，按 clubName+collegeId 匹配社团。
     */
    private Map<Long, String> loadDissolvingAppNoMap() {
        List<ClubApplication> apps = clubApplicationMapper.selectList(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplyType, ClubApplyConstants.APPLY_TYPE_DISSOLVE)
                .in(ClubApplication::getStatus,
                        ClubApplyConstants.STATUS_PENDING_COLLEGE,
                        ClubApplyConstants.STATUS_COLLEGE_APPROVED));
        if (apps.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new HashMap<>();
        for (ClubApplication app : apps) {
            if (StringUtils.isBlank(app.getClubName())) {
                continue;
            }
            LambdaQueryWrapper<SysClub> w = new LambdaQueryWrapper<SysClub>()
                    .eq(SysClub::getClubName, app.getClubName());
            if (app.getCollegeId() != null) {
                w.eq(SysClub::getCollegeId, app.getCollegeId());
            }
            SysClub club = getOne(w.last("LIMIT 1"), false);
            if (club != null && club.getId() != null) {
                result.putIfAbsent(club.getId(), app.getApplicationNo());
            }
        }
        return result;
    }

    private Map<Long, Long> loadActiveCouncilMap() {
        List<ClubCouncil> councils = clubCouncilMapper.selectList(new LambdaQueryWrapper<ClubCouncil>()
                .eq(ClubCouncil::getStatus, ClubApplyConstants.COUNCIL_IN_PROGRESS));
        Map<Long, Long> map = new HashMap<>();
        for (ClubCouncil c : councils) {
            if (c.getClubId() != null) {
                map.putIfAbsent(c.getClubId(), c.getId());
            }
        }
        return map;
    }

    private SysClubListVO toListVO(SysClub club, LoginUserDetails user,
                                   Map<Long, String> dissolvingAppNo,
                                   Map<Long, Long> councilIdByClub) {
        SysClubListVO vo = new SysClubListVO();
        vo.setId(club.getId());
        vo.setClubCode(club.getClubCode());
        vo.setClubName(club.getClubName());
        vo.setCategory(club.getCategory());
        vo.setCategoryCode(ClubCategory.prefixOfOrNull(club.getCategory()));
        vo.setCollegeId(club.getCollegeId());
        vo.setAdvisorId(club.getAdvisorId());
        vo.setDescription(club.getDescription());
        vo.setLogo(club.getLogo());
        vo.setStatus(club.getStatus());
        vo.setDissolveTime(club.getDissolveTime());
        vo.setCreateTime(club.getCreateTime());
        vo.setDissolveApplicationNo(dissolvingAppNo.get(club.getId()));
        vo.setActiveCouncilId(councilIdByClub.get(club.getId()));

        if (club.getCollegeId() != null) {
            SysCollege college = sysCollegeMapper.selectById(club.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getCollegeName());
            }
        }
        if (club.getAdvisorId() != null) {
            SysUser advisor = sysUserMapper.selectById(club.getAdvisorId());
            if (advisor != null) {
                vo.setAdvisorName(advisor.getRealName());
            }
        }

        vo.setCanDissolve(resolveCanDissolve(user, club));
        vo.setCanSignCouncil(resolveCanSignCouncil(user, club, councilIdByClub.get(club.getId())));
        return vo;
    }

    private boolean resolveCanDissolve(LoginUserDetails user, SysClub club) {
        if (club.getStatus() == null || club.getStatus() != ClubApplyConstants.CLUB_STATUS_NORMAL) {
            return false;
        }
        if (user.getEffectiveLevel() > Level.CLUB_LEADER) {
            return false;
        }
        if (Objects.equals(user.getUserId(), club.getAdvisorId())) {
            return true;
        }
        return resolvePresidentClubIds(user.getUserId()).contains(club.getId());
    }

    private boolean resolveCanSignCouncil(LoginUserDetails user, SysClub club, Long councilId) {
        if (councilId == null) {
            return false;
        }
        if (user.getEffectiveLevel() > Level.ADMIN) {
            return false;
        }
        if (club.getStatus() == null || club.getStatus() != ClubApplyConstants.CLUB_STATUS_NORMAL) {
            return false;
        }
        try {
            ClubSecurityHelper.assertCollegeInScope(sysUserRoleMapper, sysRoleMapper,
                    user.getUserId(), club.getCollegeId());
            return isSchoolAdmin(user.getUserId()) || user.getEffectiveLevel() == Level.SUPER_ADMIN;
        } catch (EIException e) {
            return false;
        }
    }

    private IPage<SysClubListVO> emptyVoPage(Page<SysClub> page) {
        Page<SysClubListVO> empty = new Page<>(page.getCurrent(), page.getSize(), 0);
        empty.setRecords(Collections.emptyList());
        return empty;
    }
}
