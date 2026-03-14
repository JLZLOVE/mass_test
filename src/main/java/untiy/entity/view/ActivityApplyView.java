
package untiy.entity.view;

import untiy.entity.ActivityApply;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 活动申请表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_apply")
public class ActivityApplyView extends ActivityApply implements Serializable {

    private static final long serialVersionUID = 1L;

    public ActivityApplyView() {
    }

    public ActivityApplyView(ActivityApply activityApply) {
        BeanUtils.copyProperties(activityApply, this);
    }
}