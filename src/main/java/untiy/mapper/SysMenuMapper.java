package untiy.mapper;

import org.apache.ibatis.annotations.Mapper;
import untiy.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 菜单权限表 Mapper 接口
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

}
