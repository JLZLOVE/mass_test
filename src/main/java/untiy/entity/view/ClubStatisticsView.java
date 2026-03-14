
package untiy.entity.view;

import untiy.entity.ClubStatistics;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 社团统计表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club_statistics")
public class ClubStatisticsView extends ClubStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    public ClubStatisticsView() {
    }

    public ClubStatisticsView(ClubStatistics clubStatistics) {
        BeanUtils.copyProperties(clubStatistics, this);
    }
}