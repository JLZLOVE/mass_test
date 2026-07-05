package untiy.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import untiy.service.impl.NoticeInfoServiceImpl;

@Component
public class NoticeScheduledTask {

    @Autowired
    private NoticeInfoServiceImpl noticeInfoService;

    /** 定时发送：每分钟扫描到期草稿 */
    @Scheduled(cron = "0 * * * * ?")
    public void publishScheduledNotices() {
        noticeInfoService.publishScheduledDrafts();
    }

    /** 置顶结束：每分钟取消过期置顶 */
    @Scheduled(cron = "0 * * * * ?")
    public void expirePinnedNotices() {
        noticeInfoService.expirePinnedNotices();
    }
}
