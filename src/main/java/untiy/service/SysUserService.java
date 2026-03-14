package untiy.service;

import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
