package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.dto.AssignRoleDTO;
import untiy.entity.SysUserRole;
import untiy.entity.vo.SysUserRoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface SysUserRoleService extends IService<SysUserRole> {

    void assign(AssignRoleDTO dto);

    void revoke(Long id);

    IPage<SysUserRoleVO> pageQuery(Map<String, Object> param, String keyword);

    List<SysUserRoleVO> listMyRoles();
}
