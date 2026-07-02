package untiy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.mapper.SysUserRoleMapper;
import untiy.service.SysRoleService;
import untiy.service.SysUserRoleService;
import untiy.service.SysUserService;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void assign(Long userId, Long roleId, Integer scopeType, Long scopeId) {
        SysRole role = sysRoleService.getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String roleCode = role.getRoleCode();
        Integer userType = user.getUserType();

        if ("PRESIDENT".equals(roleCode) && !Integer.valueOf(1).equals(userType)) {
            throw new RuntimeException("PRESIDENT角色仅允许学生(userType=1)分配");
        }
        if ("ADMIN".equals(roleCode) && !Integer.valueOf(2).equals(userType)) {
            throw new RuntimeException("ADMIN角色仅允许老师(userType=2)分配");
        }

        SysUserRole entity = new SysUserRole();
        entity.setUserId(userId);
        entity.setRoleId(roleId);
        entity.setScopeType(scopeType);
        entity.setScopeId(scopeId);
        save(entity);
    }

    @Override
    public void revoke(Long id) {
        removeById(id);
    }
}
