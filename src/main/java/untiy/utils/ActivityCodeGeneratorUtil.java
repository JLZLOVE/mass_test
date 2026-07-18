package untiy.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import untiy.entity.ActivityApply;
import untiy.entity.SysClub;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.SysClubMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 活动编号生成器。
 * 格式：{社团类别前缀}{yyyyMMdd}{4位序列号}
 * 示例：WH202607180032
 */
@Component
public class ActivityCodeGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger sequence = new AtomicInteger(0);

    @Autowired
    private ActivityApplyMapper activityApplyMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    /**
     * 根据 clubId 生成活动编号，自动解析社团类别前缀。
     */
    public String generateCode(Long clubId) {
        String prefix = resolveClubCategoryPrefix(clubId);
        String datePart = LocalDate.now().format(DATE_FORMAT);
        String code;
        do {
            int seq = sequence.updateAndGet(i -> (i + 1) % 10000);
            code = String.format("%s%s%04d", prefix, datePart, seq);
        } while (exists(code));
        return code;
    }

    /**
     * 根据社团类别解析前缀。
     */
    private String resolveClubCategoryPrefix(Long clubId) {
        SysClub club = sysClubMapper.selectById(clubId);
        if (club == null || club.getCategory() == null) {
            return "QT";
        }
        String category = club.getCategory();
        if (category.contains("文化体育")) return "WH";
        if (category.contains("学术科技")) return "XK";
        if (category.contains("志愿公益")) return "ZY";
        if (category.contains("创新创业")) return "CC";
        if (category.contains("思想政治")) return "SZ";
        return "QT";
    }

    private boolean exists(String code) {
        return activityApplyMapper.selectCount(
                new LambdaQueryWrapper<ActivityApply>().eq(ActivityApply::getActivityNo, code)) > 0;
    }
}