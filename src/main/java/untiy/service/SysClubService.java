package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.SysClub;
import untiy.entity.SysDepartment;
import untiy.entity.vo.ClubMemberCountVO;
import untiy.entity.vo.PortalClubVO;
import untiy.entity.vo.SysClubListVO;
import untiy.entity.vo.SysUserRoleVO;

import java.util.List;
import java.util.Map;

/**
 * 社团服务接口
 *
 * @author 玖
 * @since 2026-07-18
 */
public interface SysClubService extends IService<SysClub> {

    /**
     * 门户社团列表（全量返回，不分页）
     */
    List<PortalClubVO> portalList(String category);

    /**
     * 管理端分页列表。
     *
     * @param tabMode normal | dissolving | council
     */
    IPage<SysClubListVO> adminPageQuery(Map<String, Object> param, SysClub query,
                                        String keyword, String tabMode);

    /**
     * 按 clubCode 查询详情（含权限标识）。
     */
    SysClubListVO getDetailByClubCode(String clubCode);

    /**
     * 社团部门列表（架构树扁平数据，前端组树）。
     */
    List<SysDepartment> listDepartments(String clubCode);

    /**
     * 社团成员分页（基于 sys_user_role scope_type=2）。
     */
    IPage<SysUserRoleVO> pageMembers(Map<String, Object> param, String clubCode, String roleCode);

    /**
     * 批量统计成员数（distinct user_id，scope_type=2）。
     */
    List<ClubMemberCountVO> batchMemberCount(List<Long> clubIds);
}
