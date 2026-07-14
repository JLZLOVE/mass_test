package untiy.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import untiy.entity.ClubApplication;
import untiy.entity.ClubCategory;
import untiy.entity.SysClub;
import untiy.mapper.ClubApplicationMapper;
import untiy.mapper.SysClubMapper;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ClubCodeGeneratorUtil {

    /** 申请编号前缀：SQ = 申请（ShenQing） */
    private static final String APP_PREFIX = "SQ";

    private final ClubApplicationMapper clubApplicationMapper;
    private final SysClubMapper sysClubMapper;

    public ClubCodeGeneratorUtil(ClubApplicationMapper clubApplicationMapper, SysClubMapper sysClubMapper) {
        this.clubApplicationMapper = clubApplicationMapper;
        this.sysClubMapper = sysClubMapper;
    }

    /** 申请编号 = SQ + 时间戳后缀，与社团编号规则一致（如 SQ20260713441） */
    public String generateApplicationNo() {
        return ensureUniqueAppNo(APP_PREFIX + formatClubCodeSuffix(LocalDateTime.now()));
    }

    /**
     * 社团编号 = 类别英文缩写 + 创建时间戳（yyyyMMdd + H + mm，如 20230912632）。
     */
    public String generateClubCode(String category, LocalDateTime createTime) {
        String prefix = ClubCategory.prefixOf(category);
        LocalDateTime time = createTime != null ? createTime : LocalDateTime.now();
        return ensureUniqueClubCode(prefix + formatClubCodeSuffix(time));
    }

    /** 后缀：年月日 + 时(不补零) + 分，例 2023-09-12 06:32 → 20230912632 */
    static String formatClubCodeSuffix(LocalDateTime time) {
        return String.format("%04d%02d%02d%d%02d",
                time.getYear(), time.getMonthValue(), time.getDayOfMonth(),
                time.getHour(), time.getMinute());
    }

    private String ensureUniqueAppNo(String code) {
        long count = clubApplicationMapper.selectCount(
                new LambdaQueryWrapper<ClubApplication>().eq(ClubApplication::getApplicationNo, code));
        if (count == 0) {
            return code;
        }
        return ensureUniqueAppNo(APP_PREFIX + formatClubCodeSuffix(LocalDateTime.now())
                + ThreadLocalRandom.current().nextInt(1, 10));
    }

    private String ensureUniqueClubCode(String code) {
        long count = sysClubMapper.selectCount(
                new LambdaQueryWrapper<SysClub>().eq(SysClub::getClubCode, code));
        if (count == 0) {
            return code;
        }
        return ensureUniqueClubCode(code + ThreadLocalRandom.current().nextInt(1, 10));
    }
}
