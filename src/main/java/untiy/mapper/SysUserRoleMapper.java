package untiy.mapper;

import org.apache.ibatis.annotations.Mapper;
import untiy.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}
