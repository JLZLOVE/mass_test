package untiy.service;

import untiy.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户角色关联表 服务类
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 分配用户角色
     */
    void assign(Long userId, Long roleId, Integer scopeType, Long scopeId);

    /**
     * 撤销用户角色
     */
    void revoke(Long id);
}
