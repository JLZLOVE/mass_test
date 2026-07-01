package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.dto.SysUserDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户基础表 服务类
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
public interface SysUserService extends IService<SysUser> {

    void register(RegisterDTO registerDTO);

    IPage<SysUserDTO> pageQueryDTO(Map<String, Object> param, SysUser sysUser);

    IPage<SysUser> pageQueryFront(Map<String, Object> param, SysUser sysUser);

    List<SysUser> queryByCondition(SysUser sysUser);
}
