package untiy.service;

import untiy.entity.SysRoleMenu;
import untiy.entity.dto.AssignRoleMenuDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysRoleMenuService extends IService<SysRoleMenu> {

    void assign(AssignRoleMenuDTO dto);

    List<Long> listMenuIdsByRole(Long roleId);
}
