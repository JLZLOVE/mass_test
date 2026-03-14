package untiy.mapper;

import org.apache.ibatis.annotations.Mapper;
import untiy.entity.ActivitySign;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 活动签到表 Mapper 接口
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Mapper
public interface ActivitySignMapper extends BaseMapper<ActivitySign> {

}
