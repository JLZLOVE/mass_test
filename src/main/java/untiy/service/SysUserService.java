package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.dto.SysUserDTO;

import java.util.List;
import java.util.Map;

/**
 * 用户业务服务：数据范围过滤与 DTO 脱敏均在此层完成。
 */
public interface SysUserService extends IService<SysUser> {

    void register(RegisterDTO registerDTO);

    /** 分页查询（行级范围过滤 + DTO 脱敏） */
    IPage<SysUserDTO> pageQuery(Map<String, Object> param, SysUser sysUser);

    /** 详情（行级范围校验 + DTO 脱敏） */
    SysUserDTO getDetail(Long id);

    /** 新增用户 */
    void saveUser(SysUser sysUser);

    /** 批量更新（校验数据范围） */
    void updateUsers(List<SysUser> sysUsers);

    /** 批量删除（校验数据范围） */
    void deleteUsers(List<Long> ids);
}
