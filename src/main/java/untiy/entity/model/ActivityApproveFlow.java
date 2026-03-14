package untiy.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动审批流程表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("activity_approve_flow")
public class ActivityApproveFlow implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long activityId;

    /**
     * 步骤序号
     */
    private Integer step;

    /**
     * 审批角色ID
     */
    private Long approveRoleId;

    /**
     * 实际审批人ID
     */
    private Long approveUserId;

    /**
     * 1:通过 2:驳回
     */
    private Integer approveResult;

    /**
     * 审批意见
     */
    private String approveOpinion;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    private LocalDateTime createTime;


}
