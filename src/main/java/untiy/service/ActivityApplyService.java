package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import untiy.entity.ActivityApply;
import untiy.entity.dto.ActivityApproveDTO;
import untiy.entity.dto.ActivityCancelDTO;
import untiy.entity.dto.ActivityChangeDTO;
import untiy.entity.dto.ActivitySubmitDTO;
import untiy.entity.dto.ActivitySummaryDTO;
import untiy.entity.vo.ActivityApplyDetailVO;
import untiy.entity.vo.PortalActivityDetailVO;
import untiy.entity.vo.PortalActivityListVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.Map;

public interface ActivityApplyService extends IService<ActivityApply> {

    void submit(ActivitySubmitDTO dto);

    void approve(Long id, ActivityApproveDTO dto);

    void reject(Long id, ActivityApproveDTO dto);

    void requestChange(Long id, ActivityChangeDTO dto);

    void cancel(Long id, ActivityCancelDTO dto);

    void uploadSummary(Long id, ActivitySummaryDTO dto);

    ActivityApplyDetailVO getDetail(Long id);

    IPage<ActivityApply> pageQuery(Map<String, Object> param, ActivityApply query);

    /**
     * 门户活动列表（分页）
     * 仅查 approve_status = 4
     * freezeTime 不传则取当前时间；若超过「当前时间 + 5 分钟」则回退为当前时间
     * 时间范围：freezeTime - 6个月 ~ freezeTime + 2个月
     * 联查 sys_club、activity_category
     */
    Page<PortalActivityListVO> portalList(int page, int size, LocalDateTime freezeTime);

    /**
     * 门户活动详情
     * 防篡改校验：activityNo 日期与 create_time 比对，不一致时封锁 + 通知管理员 + 抛 7319
     * 联查 activity_sign_config 取签到配置（仅公开字段）
     * 不返回 GPS 坐标
     */
    PortalActivityDetailVO portalDetail(String activityNo);
}
