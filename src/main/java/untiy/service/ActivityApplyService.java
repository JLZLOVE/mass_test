package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.ActivityApply;
import untiy.entity.dto.ActivityApproveDTO;
import untiy.entity.dto.ActivityCancelDTO;
import untiy.entity.dto.ActivityChangeDTO;
import untiy.entity.dto.ActivitySubmitDTO;
import untiy.entity.dto.ActivitySummaryDTO;
import untiy.entity.vo.ActivityApplyDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
