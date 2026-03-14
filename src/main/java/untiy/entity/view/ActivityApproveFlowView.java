
package untiy.entity.view;

import untiy.entity.ActivityApproveFlow;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 活动审批流程表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_approve_flow")
public class ActivityApproveFlowView extends ActivityApproveFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    public ActivityApproveFlowView() {
    }

    public ActivityApproveFlowView(ActivityApproveFlow activityApproveFlow) {
        BeanUtils.copyProperties(activityApproveFlow, this);
    }
}