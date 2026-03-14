
package untiy.entity.view;

import untiy.entity.ActivityCategory;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 活动分类表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_category")
public class ActivityCategoryView extends ActivityCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    public ActivityCategoryView() {
    }

    public ActivityCategoryView(ActivityCategory activityCategory) {
        BeanUtils.copyProperties(activityCategory, this);
    }
}