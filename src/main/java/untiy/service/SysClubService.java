package untiy.service;

import untiy.entity.SysClub;
import untiy.entity.vo.PortalClubVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 社团服务接口
 *
 * @author 玖
 * @since 2026-07-18
 */
public interface SysClubService extends IService<SysClub> {

    /**
     * 门户社团列表（全量返回，不分页）
     * 仅查 status = 1（正常运营）
     * 可选按 category 筛选
     * 排序 category ASC, create_time DESC
     * 联查 sys_college 取 college_name
     */
    List<PortalClubVO> portalList(String category);
}