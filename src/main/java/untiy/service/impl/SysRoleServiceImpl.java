package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import untiy.entity.SysUserRole;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LevelBasedAccess;
import untiy.service.SysRoleService;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色表服务实现类
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    /** 与 {@link untiy.service.impl.AuthorServiceImpl#getAuthoritiesByUserId} 保持一致 */
    private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    // ==================== 分页查询 ====================
    @Override
    public IPage<SysRole> pageQuery(Map<String, Object> param, SysRole sysRole) {
        Page<SysRole> page = MPUtil.getPage(param);
        int currentLevel = SecurityUtils.getCurrentLevel();

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SysRole::getRoleLevel, currentLevel);

        if (sysRole != null) {
            if (StringUtils.isNotBlank(sysRole.getRoleName())) {
                wrapper.like(SysRole::getRoleName, sysRole.getRoleName());
            }
            if (StringUtils.isNotBlank(sysRole.getRoleCode())) {
                wrapper.like(SysRole::getRoleCode, sysRole.getRoleCode());
            }
        }

        return baseMapper.selectPage(page, wrapper);
    }

    // ==================== 查询详情 ====================
    @Override
    public SysRole getDetail(Long id) {
        if (id == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        SysRole role = getById(id);
        if (role == null) {
            throw new EIException(ErrorConfig.ROLE_NOT_FOUND_CODE, ErrorConfig.ROLE_NOT_FOUND_MSG);
        }
        int currentLevel = SecurityUtils.getCurrentLevel();
        if (role.getRoleLevel() != null && role.getRoleLevel() < currentLevel) {
            log.warn("用户等级 {} 尝试查看等级 {} 的角色 id={}（越权）", currentLevel, role.getRoleLevel(), id);
            LevelBasedAccess.checkViewable(currentLevel, role.getRoleLevel());
        }
        return role;
    }

    // ==================== 新增角色 ====================
    @Transactional
    @Override
    public void saveRole(SysRole sysRole) {
        if (sysRole == null || StringUtils.isBlank(sysRole.getRoleCode())) {
            throw new EIException(ErrorConfig.ROLE_CODE_BLANK_CODE, ErrorConfig.ROLE_CODE_BLANK_MSG);
        }
        if (sysRole.getRoleLevel() == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        int currentLevel = SecurityUtils.getCurrentLevel();
        if (sysRole.getRoleLevel() < currentLevel) {
            log.warn("用户等级 {} 尝试创建等级 {} 的角色 code={}（越权）",
                    currentLevel, sysRole.getRoleLevel(), sysRole.getRoleCode());
            LevelBasedAccess.checkNewLevel(currentLevel, sysRole.getRoleLevel());
        }
        save(sysRole);
    }

    // ==================== 批量更新角色 ====================
    @Transactional
    @Override
    public void updateRoles(List<SysRole> sysRoles) {
        if (sysRoles == null || sysRoles.isEmpty()) {
            return;
        }
        for (SysRole role : sysRoles) {
            doUpdateRole(role);
        }
    }

    // ==================== 更新单个角色 ====================
    @Transactional
    @Override
    public void updateRole(SysRole sysRole) {
        doUpdateRole(sysRole);
    }

    // ==================== 删除单个角色 ====================
    @Transactional
    @Override
    public void deleteById(Long id) {
        doDeleteById(id);
    }

    // ==================== 批量删除角色 ====================
    @Transactional
    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> distinctIds = ids.stream().distinct().collect(Collectors.toList());
        for (Long id : distinctIds) {
            doDeleteById(id);
        }
    }

    // ==================== 私有辅助方法 ====================

    private void doUpdateRole(SysRole sysRole) {
        if (sysRole == null || sysRole.getId() == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        int currentLevel = SecurityUtils.getCurrentLevel();
        SysRole originalRole = getById(sysRole.getId());
        if (originalRole == null) {
            throw new EIException(ErrorConfig.ROLE_NOT_FOUND_CODE, ErrorConfig.ROLE_NOT_FOUND_MSG);
        }
        if (originalRole.getRoleLevel() != null && originalRole.getRoleLevel() < currentLevel) {
            log.warn("用户等级 {} 尝试修改等级 {} 的角色 id={}（越权）",
                    currentLevel, originalRole.getRoleLevel(), sysRole.getId());
            LevelBasedAccess.checkOperable(currentLevel, originalRole.getRoleLevel());
        }
        if (sysRole.getRoleLevel() != null && sysRole.getRoleLevel() < currentLevel) {
            log.warn("用户等级 {} 尝试将角色 id={} 提升为等级 {}（越权）",
                    currentLevel, sysRole.getId(), sysRole.getRoleLevel());
            LevelBasedAccess.checkNewLevel(currentLevel, sysRole.getRoleLevel());
        }

        LambdaUpdateWrapper<SysRole> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysRole::getId, sysRole.getId());
        if (sysRole.getRoleName() != null) {
            updateWrapper.set(SysRole::getRoleName, sysRole.getRoleName());
        }
        if (StringUtils.isNotBlank(sysRole.getRoleCode())) {
            updateWrapper.set(SysRole::getRoleCode, sysRole.getRoleCode());
        }
        if (sysRole.getRoleLevel() != null) {
            updateWrapper.set(SysRole::getRoleLevel, sysRole.getRoleLevel());
        }
        if (sysRole.getDataScope() != null) {
            updateWrapper.set(SysRole::getDataScope, sysRole.getDataScope());
        }
        if (sysRole.getDescription() != null) {
            updateWrapper.set(SysRole::getDescription, sysRole.getDescription());
        }
        if (sysRole.getStatus() != null) {
            updateWrapper.set(SysRole::getStatus, sysRole.getStatus());
        }
        update(updateWrapper);
    }

    private void doDeleteById(Long id) {
        if (id == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, ErrorConfig.BAD_REQUEST_MSG);
        }
        int currentLevel = SecurityUtils.getCurrentLevel();
        SysRole role = getById(id);
        if (role == null) {
            throw new EIException(ErrorConfig.ROLE_NOT_FOUND_CODE, ErrorConfig.ROLE_NOT_FOUND_MSG);
        }
        if (role.getRoleLevel() != null && role.getRoleLevel() < currentLevel) {
            log.warn("用户等级 {} 尝试删除等级 {} 的角色 id={} code={}（越权）",
                    currentLevel, role.getRoleLevel(), id, role.getRoleCode());
            LevelBasedAccess.checkOperable(currentLevel, role.getRoleLevel());
        }
        checkRoleNotInUse(role.getRoleCode());
        removeUserRoleAssociations(id);
        removeById(id);
    }

    private void removeUserRoleAssociations(Long roleId) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId));
    }

    /**
     * 校验当前用户是否拥有指定角色码，若拥有则抛出异常（不能删除自己正在使用的角色）。
     * 权限字符串格式与 {@link AuthorServiceImpl} 一致：{@code ROLE_} + roleCode。
     */
    private void checkRoleNotInUse(String roleCode) {
        if (StringUtils.isBlank(roleCode)) {
            return;
        }
        String expectedAuthority = buildRoleAuthority(roleCode);
        Collection<? extends GrantedAuthority> authorities = SecurityUtils.getCurrentUser().getAuthorities();
        for (GrantedAuthority auth : authorities) {
            if (expectedAuthority.equals(auth.getAuthority())) {
                throw new EIException(ErrorConfig.ROLE_IN_USE_CODE, ErrorConfig.ROLE_IN_USE_MSG);
            }
        }
    }

    private static String buildRoleAuthority(String roleCode) {
        return ROLE_AUTHORITY_PREFIX + roleCode;
    }
}
