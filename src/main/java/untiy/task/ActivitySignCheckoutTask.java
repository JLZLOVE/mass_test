package untiy.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import untiy.service.ActivitySignService;

@Component
public class ActivitySignCheckoutTask {

    @Autowired
    private ActivitySignService activitySignService;

    /** 活动结束后自动签退（不标记早退） */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoCheckout() {
        activitySignService.autoCheckoutExpired();
    }
}
