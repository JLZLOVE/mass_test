package untiy.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import untiy.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import untiy.entity.vo.SysUserRoleVO;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    IPage<SysUserRoleVO> selectPageWithDetail(Page<SysUserRoleVO> page,
                                              @Param("userIds") List<Long> userIds,
                                              @Param("keyword") String keyword);

    List<SysUserRoleVO> selectListByUserId(@Param("userId") Long userId);
}
