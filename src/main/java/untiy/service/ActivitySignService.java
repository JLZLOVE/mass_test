package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.ActivitySignConfig;
import untiy.entity.dto.*;
import untiy.entity.vo.SignRecordVO;
import untiy.entity.vo.SignStatsVO;

import java.util.Map;

public interface ActivitySignService extends com.baomidou.mybatisplus.extension.service.IService<untiy.entity.ActivitySign> {

    void saveConfig(SignConfigDTO dto);

    void updateConfig(SignConfigDTO dto);

    ActivitySignConfig getConfig(Long activityId);

    void sign(Long activityId, SignActionDTO dto);

    void adminSign(Long activityId, AdminSignDTO dto);

    void checkout(Long activityId);

    Long applyMakeup(Long activityId, MakeupApplyDTO dto);

    void approveMakeup(Long applyId, MakeupApproveDTO dto);

    SignStatsVO stats(Long activityId);

    IPage<SignRecordVO> listRecords(Long activityId, Map<String, Object> param);

    void autoCheckoutExpired();
}
