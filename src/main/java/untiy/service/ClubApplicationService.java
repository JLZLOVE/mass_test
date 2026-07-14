package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.ClubApplication;
import untiy.entity.dto.ClubAdminApproveDTO;
import untiy.entity.dto.ClubCollegeApproveDTO;
import untiy.entity.dto.ClubCreateApplyDTO;
import untiy.entity.dto.ClubDissolveApplyDTO;
import untiy.entity.vo.ClubApplicationDetailVO;

import java.util.Map;

public interface ClubApplicationService extends IService<ClubApplication> {

    String createApply(ClubCreateApplyDTO dto);

    void dissolveApply(ClubDissolveApplyDTO dto);

    IPage<ClubApplication> pageQuery(Map<String, Object> param, ClubApplication query, String username);

    ClubApplicationDetailVO getDetailByApplicationNo(String applicationNo);

    ClubApplicationDetailVO getDetailByUsername(String username);

    void approveCollege(ClubCollegeApproveDTO dto);

    void approveAdmin(ClubAdminApproveDTO dto);
}
