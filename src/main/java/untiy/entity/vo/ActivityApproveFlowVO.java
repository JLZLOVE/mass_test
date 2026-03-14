package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 活动审批流程表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class ActivityApproveFlowVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 
             */
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
            /**
             * 
             */
            private LocalDateTime createTime;
}