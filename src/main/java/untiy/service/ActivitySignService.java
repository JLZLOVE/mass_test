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

    ActivitySignConfig getConfig(String activityNo);

    void sign(String activityNo, SignActionDTO dto);

    void adminSign(String activityNo, AdminSignDTO dto);

    void checkout(String activityNo);

    Long applyMakeup(String activityNo, MakeupApplyDTO dto);

    void approveMakeup(Long applyId, MakeupApproveDTO dto);

    SignStatsVO stats(String activityNo);

    IPage<SignRecordVO> listRecords(String activityNo, Map<String, Object> param);

    void autoCheckoutExpired();
}
