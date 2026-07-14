package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import untiy.entity.SysClub;
import untiy.entity.constants.ClubApplyConstants;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysClubMapper;

/**
 * 社团对外编号 clubCode 查询（不暴露数据库 id）。
 */
public final class ClubLookupHelper {

    private ClubLookupHelper() {
    }

    public static SysClub findByClubCode(SysClubMapper mapper, String clubCode) {
        if (clubCode == null || clubCode.isBlank()) {
            return null;
        }
        return mapper.selectOne(new LambdaQueryWrapper<SysClub>()
                .eq(SysClub::getClubCode, clubCode.trim()));
    }

    public static SysClub requireNormalByClubCode(SysClubMapper mapper, String clubCode) {
        SysClub club = findByClubCode(mapper, clubCode);
        if (club == null) {
            throw new EIException(ErrorConfig.CLUB_NOT_FOUND_CODE, ErrorConfig.CLUB_NOT_FOUND_MSG);
        }
        if (club.getStatus() == null || club.getStatus() != ClubApplyConstants.CLUB_STATUS_NORMAL) {
            throw new EIException(ErrorConfig.CLUB_NOT_NORMAL_CODE, ErrorConfig.CLUB_NOT_NORMAL_MSG);
        }
        return club;
    }
}
