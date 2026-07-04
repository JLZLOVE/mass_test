package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.dto.SysUserDTO;
import untiy.entity.dto.ToggleStatusDTO;

import java.util.List;
import java.util.Map;

/**
 * 用户业务服务：数据范围过滤与 DTO 脱敏均在此层完成。
 */
public interface SysUserService extends IService<SysUser> {
    //禁用
//    void enableOrDisable(String username, Integer status);

    //注册
    void register(RegisterDTO registerDTO);

    /**
     * 分页查询（行级范围过滤 + DTO 脱敏）
     */
    IPage<SysUserDTO> pageQuery(Map<String, Object> param, SysUser sysUser);

    /**
     * 详情（行级范围校验 + DTO 脱敏）
     */
    SysUserDTO getDetail(String username);

    /**
     * 新增用户
     */
    void saveUser(SysUser sysUser);

    /**
     * 批量更新（校验数据范围）
     */
    void updateUsers(List<SysUser> sysUsers);

    /**
     * 批量删除（校验数据范围）
     */
    void deleteUsers(List<String> usernames);

    /**
     * 根据username删除用户（校验数据范围）
     */
    void deleteByUsername(String username);

    void updateUser(SysUser sysUsers);

    /** 批量启用/禁用（管理员、数据范围内、禁用时不可含自己） */
    void toggleStatus(ToggleStatusDTO request);

    /** 分页查询已禁用用户（status=0，关键词模糊，数据范围过滤） */
    IPage<SysUserDTO> listDisabled(Map<String, Object> param, String keyword);
}
