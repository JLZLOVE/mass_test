
package untiy.entity.view;

import untiy.entity.ActivitySign;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 活动签到表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_sign")
public class ActivitySignView extends ActivitySign implements Serializable {

    private static final long serialVersionUID = 1L;

    public ActivitySignView() {
    }

    public ActivitySignView(ActivitySign activitySign) {
        BeanUtils.copyProperties(activitySign, this);
    }
}