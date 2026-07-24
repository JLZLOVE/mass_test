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

        UserRoleScopeHelper.validateScope(role.getDataScope(), dto.getScopeType(), dto.getScopeId(),
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
