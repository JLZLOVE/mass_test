package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.SysClub;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.dto.AssignRoleDTO;
import untiy.entity.vo.SysUserRoleVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.DataScopeHelper;
import untiy.security.LevelBasedAccess;
import untiy.security.UserRoleScopeHelper;
import untiy.security.UserScopeResolver;
import untiy.security.UserSecurityHelper;
import untiy.service.SysRoleService;
import untiy.service.SysUserRoleService;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private SysDepartmentMapper sysDepartmentMapper;

    @Transactional
    @Override
    public void assign(AssignRoleDTO dto) {
        if (dto == null || StringUtils.isBlank(dto.getUsername()) || dto.getRoleId() == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }

        // 前端传 clubName → 服务端内部转为 scopeId
        resolveClubName(dto);

        SysUser user = UserSecurityHelper.findActiveInScopeByUsername(sysUserMapper, dto.getUsername());

        // role_code 为常量（如 ADVISOR / ADVISOR_SZ_XXX），分配时不动态拼接；社团/学院上下文由 scope 表达
        SysRole role = sysRoleService.getById(dto.getRoleId());
        if (role == null) {
            throw new EIException(ErrorConfig.ROLE_NOT_FOUND_CODE, ErrorConfig.ROLE_NOT_FOUND_MSG);
        }

        int currentLevel = SecurityUtils.getCurrentLevel();
        int targetRoleLevel = UserScopeResolver.resolveEffectiveLevel(Collections.singletonList(role));
        if (targetRoleLevel < currentLevel) {
            log.warn("用户等级 {} 尝试分配等级 {} 的角色 id={}（越权）", currentLevel, targetRoleLevel, dto.getRoleId());
            LevelBasedAccess.checkNewLevel(currentLevel, targetRoleLevel);
        }

        Integer dataScope = UserRoleScopeHelper.resolveDataScope(role);
        if (dataScope == null) {
            log.error("角色 id={} roleCode={} dataScope 为空且无法按 role_code 推断", role.getId(), role.getRoleCode());
            throw new EIException(ErrorConfig.ROLE_SCOPE_NOT_CONFIGURED_CODE,
                    ErrorConfig.ROLE_SCOPE_NOT_CONFIGURED_MSG + "（roleCode=" + role.getRoleCode() + "）");
        }

        assertUserTypeMatchesRole(user, role);

        log.info("分配角色 user={} roleId={} roleCode={} dataScope={} scopeType={} scopeId={}",
                dto.getUsername(), role.getId(), role.getRoleCode(), dataScope, dto.getScopeType(), dto.getScopeId());
        UserRoleScopeHelper.validateScope(dataScope, dto.getScopeType(), dto.getScopeId(),
                sysCollegeMapper, sysClubMapper, sysDepartmentMapper);
        UserRoleScopeHelper.assertNoDuplicateAssignment(sysUserRoleMapper,
                user.getId(), role.getId(), dto.getScopeType(), dto.getScopeId());

        SysUserRole entity = new SysUserRole();
        entity.setUserId(user.getId());
        entity.setRoleId(role.getId());
        entity.setScopeType(dto.getScopeType());
        entity.setScopeId(dto.getScopeId());
        entity.setCreateTime(LocalDateTime.now());
        save(entity);
    }

    /**
     * 角色与用户类型匹配：社长/部长→学生；指导老师/管理员→教师；MEMBER 无限制。
     */
    private void assertUserTypeMatchesRole(SysUser user, SysRole role) {
        Integer required = requiredUserTypeForRole(role);
        if (required == null) {
            return;
        }
        Integer actual = user.getUserType();
        if (actual == null || !actual.equals(required)) {
            String need = required == 1 ? "学生" : "教师";
            String got = actual == null ? "未知" : (actual == 1 ? "学生" : actual == 2 ? "教师" : "其他");
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE,
                    "「" + role.getRoleName() + "」角色只能分配给" + need + "，当前用户身份为" + got);
        }
    }

    private Integer requiredUserTypeForRole(SysRole role) {
        if (role == null) {
            return null;
        }
        String code = role.getRoleCode() == null ? "" : role.getRoleCode().trim().toUpperCase(java.util.Locale.ROOT);
        String name = role.getRoleName() == null ? "" : role.getRoleName();
        if (ClubApplyConstants.ROLE_CLUB_PRESIDENT.equals(code)
                || ClubApplyConstants.ROLE_CLUB_MINISTER.equals(code)
                || name.contains("社长") || name.contains("部长")) {
            return 1;
        }
        if (ClubApplyConstants.ROLE_ADMIN.equals(code)
                || ClubApplyConstants.ROLE_SUPER_ADMIN.equals(code)
                || UserScopeResolver.isAdvisorRoleCode(code)
                || name.contains("指导") || name.contains("管理员")) {
            return 2;
        }
        return null;
    }

    @Transactional
    @Override
    public void revoke(Long id) {
        if (id == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        SysUserRole association = getById(id);
        if (association == null) {
            throw new EIException(ErrorConfig.USER_ROLE_NOT_FOUND_CODE, ErrorConfig.USER_ROLE_NOT_FOUND_MSG);
        }

        SysRole role = sysRoleService.getById(association.getRoleId());
        if (role == null) {
            throw new EIException(ErrorConfig.ROLE_NOT_FOUND_CODE, ErrorConfig.ROLE_NOT_FOUND_MSG);
        }

        int currentLevel = SecurityUtils.getCurrentLevel();
        int targetRoleLevel = UserScopeResolver.resolveEffectiveLevel(Collections.singletonList(role));
        if (targetRoleLevel < currentLevel) {
            log.warn("用户等级 {} 尝试撤销等级 {} 的角色关联 id={}（越权）", currentLevel, targetRoleLevel, id);
            LevelBasedAccess.checkOperable(currentLevel, targetRoleLevel);
        }

        assertNotRevokingOwnRole(role.getRoleCode());
        removeById(id);
    }

    @Override
    public IPage<SysUserRoleVO> pageQuery(Map<String, Object> param, String keyword) {
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        DataScopeHelper.applySysUserScope(userWrapper);

        List<Long> userIds = sysUserMapper.selectList(userWrapper.select(SysUser::getId)).stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());

        Page<SysUserRoleVO> page = MPUtil.getPage(param);
        if (userIds.isEmpty()) {
            return page;
        }
        return sysUserRoleMapper.selectPageWithDetail(page, userIds, keyword);
    }

    @Override
    public List<SysUserRoleVO> listMyRoles() {
        Long userId = SecurityUtils.getCurrentUser().getUserId();
        return sysUserRoleMapper.selectListByUserId(userId);
    }

    @Override
    public List<SysUserRoleVO> listByUsername(String username) {
        SysUser user = UserSecurityHelper.requireInScopeByUsername(sysUserMapper, username);
        return sysUserRoleMapper.selectListByUserId(user.getId());
    }

    private void resolveClubName(AssignRoleDTO dto) {
        if (StringUtils.isNotBlank(dto.getClubName())) {
            SysClub club = sysClubMapper.selectOne(new LambdaQueryWrapper<SysClub>().eq(SysClub::getClubName, dto.getClubName()));
            if (club == null) {
                throw new EIException(ErrorConfig.CLUB_NOT_FOUND_CODE, ErrorConfig.CLUB_NOT_FOUND_MSG);
            }
            dto.setScopeId(club.getId());
            dto.setScopeType(2);
        }
    }

    private void assertNotRevokingOwnRole(String roleCode) {
        if (StringUtils.isBlank(roleCode)) {
            return;
        }
        String expected = ROLE_AUTHORITY_PREFIX + roleCode;
        for (GrantedAuthority auth : SecurityUtils.getCurrentUser().getAuthorities()) {
            if (expected.equals(auth.getAuthority())) {
                throw new EIException(ErrorConfig.ROLE_REVOKE_SELF_CODE, ErrorConfig.ROLE_REVOKE_SELF_MSG);
            }
        }
    }
}
