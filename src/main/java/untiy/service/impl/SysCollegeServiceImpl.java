package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysDepartment;
import untiy.entity.SysRole;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.Level;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.UserScopeResolver;
import untiy.service.SysCollegeService;
import untiy.utils.SecurityUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysCollegeServiceImpl implements SysCollegeService {

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysDepartmentMapper sysDepartmentMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<SysCollege> listForCurrentUser(String keyword) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        LambdaQueryWrapper<SysCollege> wrapper = new LambdaQueryWrapper<SysCollege>()
                .eq(SysCollege::getStatus, 1)
                .orderByAsc(SysCollege::getCollegeName);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(SysCollege::getCollegeName, keyword.trim());
        }

        int level = user.getEffectiveLevel();
        if (level <= Level.ADMIN) {
            return sysCollegeMapper.selectList(wrapper);
        }

        Long collegeId = resolveScopedCollegeId(user);
        if (collegeId == null) {
            return Collections.emptyList();
        }
        wrapper.eq(SysCollege::getId, collegeId);
        return sysCollegeMapper.selectList(wrapper);
    }

    private Long resolveScopedCollegeId(LoginUserDetails user) {
        int level = user.getEffectiveLevel();
        if (level == Level.CLUB_LEADER) {
            Set<Long> presidentRoleIds = UserScopeResolver.loadActiveRoles(sysUserRoleMapper, sysRoleMapper, user.getUserId())
                    .stream()
                    .filter(r -> ClubApplyConstants.ROLE_CLUB_PRESIDENT.equals(r.getRoleCode()))
                    .map(SysRole::getId)
                    .collect(Collectors.toSet());
            Long clubId = user.getPrimaryClubId();
            if (!presidentRoleIds.isEmpty()) {
                clubId = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, user.getUserId())
                                .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_CLUB)
                                .in(SysUserRole::getRoleId, presidentRoleIds))
                        .stream()
                        .map(SysUserRole::getScopeId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(user.getPrimaryClubId());
            }
            if (clubId == null) {
                return null;
            }
            SysClub club = sysClubMapper.selectById(clubId);
            return club != null ? club.getCollegeId() : null;
        }
        if (level == Level.DEPT_LEADER) {
            Long deptId = user.getPrimaryDepartmentId();
            if (deptId == null) {
                return null;
            }
            SysDepartment dept = sysDepartmentMapper.selectById(deptId);
            if (dept == null || dept.getClubId() == null) {
                return null;
            }
            SysClub club = sysClubMapper.selectById(dept.getClubId());
            return club != null ? club.getCollegeId() : null;
        }
        return null;
    }
}
