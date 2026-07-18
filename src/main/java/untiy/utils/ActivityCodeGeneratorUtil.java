package untiy.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import untiy.entity.ActivityApply;
import untiy.entity.ClubCategory;
import untiy.entity.SysClub;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.SysClubMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 活动编号生成器。
 * <p>
 * 统一格式：{社团6类前缀}{yyyyMMddHHmm}{5位随机}（19位）
 * 例：WH20260718143082739
 */
@Slf4j
@Component
public class ActivityCodeGeneratorUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final SysClubMapper sysClubMapper;
    private final ActivityApplyMapper activityApplyMapper;

    public ActivityCodeGeneratorUtil(SysClubMapper sysClubMapper, ActivityApplyMapper activityApplyMapper) {
        this.sysClubMapper = sysClubMapper;
        this.activityApplyMapper = activityApplyMapper;
    }

    /**
     * 根据社团 ID 生成活动编号。
     * 前缀从社团类别自动解析（通过 ClubCategory.prefixOf）。
     */
    public String generateCode(Long clubId) {
        SysClub club = sysClubMapper.selectById(clubId);
        if (club == null) {
            throw new IllegalArgumentException("社团不存在: " + clubId);
        }
        String prefix = ClubCategory.prefixOf(club.getCategory());
        return ensureUnique(prefix, LocalDateTime.now());
    }

    /**
     * 通用编号生成：{prefix}{yyyyMMddHHmm}{5位随机}
     */
    public String generate(String prefix, LocalDateTime createTime) {
        LocalDateTime now = createTime != null ? createTime : LocalDateTime.now();
        String timePart = now.format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(100_000);
        return prefix + timePart + String.format("%05d", random);
    }

    /**
     * 确保 activityNo 在 activity_apply 表中唯一
     */
    public String ensureUnique(String prefix, LocalDateTime createTime) {
        String code;
        do {
            code = generate(prefix, createTime);
        } while (activityApplyMapper.selectCount(
                new LambdaQueryWrapper<ActivityApply>().eq(ActivityApply::getActivityNo, code)) > 0);
        return code;
    }
}