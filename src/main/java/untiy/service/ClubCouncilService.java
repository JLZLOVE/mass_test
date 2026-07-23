package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.ClubCouncil;
import untiy.entity.dto.CouncilInitiateDTO;
import untiy.entity.vo.ClubCouncilDetailVO;

import java.util.Map;

public interface ClubCouncilService extends IService<ClubCouncil> {

    void initiate(CouncilInitiateDTO dto);

    void sign(Long councilId);

    /** 合议中列表（可按 clubId 过滤） */
    IPage<ClubCouncilDetailVO> pageQuery(Map<String, Object> param, Long clubId);

    /** 按合议 ID 或社团 ID 查详情（优先 councilId） */
    ClubCouncilDetailVO getDetail(Long councilId, Long clubId);
}
