package untiy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import untiy.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import untiy.entity.view.SysUserView;

import java.util.HashMap;

/**
 * <p>
 * 用户基础表 Mapper 接口
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    boolean addNewUser(HashMap<String, Object> map);


    SysUserView selectByStatusId(String nameid);
}
