package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import untiy.entity.NoticeInfo;
import untiy.entity.dto.NoticeSendDTO;
import untiy.entity.vo.NoticeDetailVO;
import untiy.entity.vo.PortalNoticeDetailVO;
import untiy.entity.vo.PortalNoticeListVO;

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

    /**
     * 门户通知列表（分页）
     * 仅查 receiver_type=1 且 status=1
     * 批量查 readCount
     * summary 截取逻辑：<50 不截取，50~150 取前20%，>150 取前150
     */
    Page<PortalNoticeListVO> portalList(int page, int size);

    /**
     * 门户通知详情
     * 不存在抛 6401
     * viewCount + 1（异步更新）
     * 附件：仅 attachment_min_level 为 null 或 0 时返回
     */
    PortalNoticeDetailVO portalDetail(String noticeNo);
}
