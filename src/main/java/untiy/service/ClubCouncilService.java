package untiy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.ClubCouncil;
import untiy.entity.dto.CouncilInitiateDTO;

public interface ClubCouncilService extends IService<ClubCouncil> {

    void initiate(CouncilInitiateDTO dto);

    void sign(Long councilId);
}
