package untiy.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import untiy.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import untiy.entity.vo.ClubMemberCountVO;
import untiy.entity.vo.SysUserRoleVO;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    IPage<SysUserRoleVO> selectPageWithDetail(Page<SysUserRoleVO> page,
                                              @Param("userIds") List<Long> userIds,
                                              @Param("keyword") String keyword);

    List<SysUserRoleVO> selectListByUserId(@Param("userId") Long userId);

    /**
     * 按社团 scope 分页查询成员角色。
     */
    IPage<SysUserRoleVO> selectPageByClubScope(Page<SysUserRoleVO> page,
                                               @Param("clubId") Long clubId,
                                               @Param("roleCode") String roleCode);

    /**
     * 批量统计社团成员数（distinct user_id）。
     */
    List<ClubMemberCountVO> countMembersByClubIds(@Param("clubIds") List<Long> clubIds);
}
