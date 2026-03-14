package untiy.entity.vo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 活动申请表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class ActivityApplyVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 活动编号
             */
            private String activityNo;
            /**
             * 主办社团
             */
            private Long clubId;
            /**
             * 活动名称
             */
            private String activityName;
            /**
             * 活动分类
             */
            private Long categoryId;
            /**
             * 1:日常活动 2:比赛 3:演出 4:讲座 5:其他
             */
            private Integer activityType;
            /**
             * 开始时间
             */
            private LocalDateTime startTime;
            /**
             * 结束时间
             */
            private LocalDateTime endTime;
            /**
             * 活动地点
             */
            private String location;
            /**
             * 详细位置（用于定位）
             */
            private String locationDetail;
            /**
             * 预计参与人数
             */
            private Integer expectedPeople;
            /**
             * 预算金额
             */
            private BigDecimal budget;
            /**
             * 活动内容
             */
            private String activityContent;
            /**
             * 安全预案
             */
            private String safetyPlan;
            /**
             * 申请人
             */
            private Long applyUserId;
            /**
             * 
             */
            private LocalDateTime applyTime;
            /**
             * 当前审批步骤
             */
            private Integer currentApproveStep;
            /**
             * 1:草稿 2:待审批 3:审批中 4:已通过 5:已驳回 6:已取消
             */
            private Integer approveStatus;
            /**
             * 驳回原因
             */
            private String rejectReason;
            /**
             * 
             */
            private LocalDateTime createTime;
            /**
             * 
             */
            private LocalDateTime updateTime;
}