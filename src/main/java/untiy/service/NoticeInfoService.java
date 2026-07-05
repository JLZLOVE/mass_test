package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.NoticeInfo;
import untiy.entity.dto.NoticeSendDTO;
import untiy.entity.vo.NoticeDetailVO;

import java.util.Map;

public interface NoticeInfoService extends com.baomidou.mybatisplus.extension.service.IService<NoticeInfo> {

    Long send(NoticeSendDTO dto);

    Long saveDraft(NoticeSendDTO dto);

    void publishNow(Long id);

    void withdraw(Long id);

    NoticeDetailVO getDetail(Long id);

    void confirmRead(Long id);

    IPage<NoticeInfo> myInbox(Map<String, Object> param);

    IPage<NoticeInfo> mySent(Map<String, Object> param);

    NoticeDetailVO readStats(Long id);
}
