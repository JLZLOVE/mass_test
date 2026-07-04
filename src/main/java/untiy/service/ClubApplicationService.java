package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.ClubApplication;
import untiy.entity.dto.ClubApproveDTO;
import untiy.entity.dto.ClubCreateApplyDTO;
import untiy.entity.dto.ClubDissolveApplyDTO;
import untiy.entity.vo.ClubApplicationDetailVO;

import java.util.Map;

public interface ClubApplicationService extends IService<ClubApplication> {

    void createApply(ClubCreateApplyDTO dto);

    void dissolveApply(ClubDissolveApplyDTO dto);

    IPage<ClubApplication> pageQuery(Map<String, Object> param, ClubApplication query);

    ClubApplicationDetailVO getDetail(Long id);

    void approveCollege(Long id, ClubApproveDTO dto);

    void approveAdmin(Long id, ClubApproveDTO dto);
}
