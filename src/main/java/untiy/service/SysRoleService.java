package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> pageQuery(Map<String, Object> param, SysRole sysRole);

    SysRole getDetail(Long id);

    void saveRole(SysRole sysRole);

    void updateRoles(List<SysRole> sysRoles);

    void updateRole(SysRole sysRole);

    void deleteById(Long id);

    void deleteByIds(List<Long> ids);
}
