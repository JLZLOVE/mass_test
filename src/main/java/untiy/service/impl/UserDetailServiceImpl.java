package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.UserScopeResolver;
import untiy.service.AuthorService;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthorService authorService;

    @Transactional(readOnly = true)
    public LoginUserDetails loadUserById(String id, String password) {
        log.info("进入 loadUserById，参数：{}", id);
        if (Objects.isNull(password) || Objects.isNull(id) || id.isEmpty() || password.isEmpty()) {
            throw new EIException(ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE, ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_MSG);
        }
        SysUser sysUser = sysUserMapper.selectByStatusId(id);
        if (Objects.isNull(sysUser)) {
            throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE, ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }
        return buildLoginUserDetails(sysUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername：{}", username);
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (Objects.isNull(sysUser)) {
            log.error("用户不存在：{}", username);
            throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE, ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }
        return buildLoginUserDetails(sysUser);
    }

    /**
     * 一次性加载：用户 → 角色 → 最小 role_level 映射 → scope_id（社团/部门）。
     */
    private LoginUserDetails buildLoginUserDetails(SysUser sysUser) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));

        List<SysRole> roles = loadActiveRoles(userRoles);
        int effectiveLevel = UserScopeResolver.resolveEffectiveLevel(roles);
        Long primaryClubId = UserScopeResolver.resolvePrimaryClubId(userRoles, roles);
        Long primaryDepartmentId = UserScopeResolver.resolvePrimaryDepartmentId(userRoles, roles);

        log.info("用户 {} 权限等级={}，clubId={}，deptId={}",
                sysUser.getUsername(), effectiveLevel, primaryClubId, primaryDepartmentId);

        return new LoginUserDetails(sysUser, authorService, effectiveLevel, primaryClubId, primaryDepartmentId);
    }

    private List<SysRole> loadActiveRoles(List<SysUserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, 1));
    }
}
