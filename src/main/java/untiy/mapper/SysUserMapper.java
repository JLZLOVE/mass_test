package untiy.mapper;

import com.baomidou.mybatisplus.annotation.TableId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import untiy.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import untiy.entity.view.SysUserView;

import java.util.HashMap;
import java.util.List;

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
//
//    boolean addNewUser(HashMap<String, Object> map);


//    SysUserView selectByStatusId(@Param("nameId") String nameId);

    SysUser selectByUsername(@Param("username") String username);

    int deleteByUsername(@Param("username") String username);

    int deleteByUsernames(@Param("usernames") List<String> usernames);
}
