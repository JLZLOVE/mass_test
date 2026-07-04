package untiy.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import untiy.entity.ClubApplication;
import untiy.mapper.ClubApplicationMapper;
import untiy.mapper.SysClubMapper;
import untiy.entity.SysClub;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ClubCodeGeneratorUtil {

    private static final String APP_PREFIX = "APP";
    private static final String CLUB_PREFIX = "CLUB";

    private final ClubApplicationMapper clubApplicationMapper;
    private final SysClubMapper sysClubMapper;

    public ClubCodeGeneratorUtil(ClubApplicationMapper clubApplicationMapper, SysClubMapper sysClubMapper) {
        this.clubApplicationMapper = clubApplicationMapper;
        this.sysClubMapper = sysClubMapper;
    }

    public String generateApplicationNo() {
        return ensureUniqueAppNo(buildCode(APP_PREFIX));
    }

    public String generateClubCode() {
        return ensureUniqueClubCode(buildCode(CLUB_PREFIX));
    }

    private String buildCode(String prefix) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix + datePart + randomNum;
    }

    private String ensureUniqueAppNo(String code) {
        long count = clubApplicationMapper.selectCount(
                new LambdaQueryWrapper<ClubApplication>().eq(ClubApplication::getApplicationNo, code));
        if (count == 0) {
            return code;
        }
        return ensureUniqueAppNo(APP_PREFIX + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ThreadLocalRandom.current().nextInt(1000, 10000));
    }

    private String ensureUniqueClubCode(String code) {
        long count = sysClubMapper.selectCount(
                new LambdaQueryWrapper<SysClub>().eq(SysClub::getClubCode, code));
        if (count == 0) {
            return code;
        }
        return ensureUniqueClubCode(CLUB_PREFIX + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ThreadLocalRandom.current().nextInt(1000, 10000));
    }
}
