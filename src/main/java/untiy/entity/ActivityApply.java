package untiy.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import untiy.annotion.StartBeforeEnd;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * <p>
 * 活动申请表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@StartBeforeEnd(message = "开始时间必须早于结束时间")
public class ActivityApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动编号
     */
    @NotBlank(message = "活动编号不可为空")
    private String activityNo;

    /**
     * 主办社团
     */
    @NotNull(message = "社团编号不可为空")
    private Long clubId;

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名是必填项")
    private String activityName;

    /**
     * 活动分类
     */
    @NotNull(message = "分类不可为空")
    private Long categoryId;

    /**
     * 1:日常活动 2:比赛 3:演出 4:讲座 5:其他
     */
    @NotNull(message = "活动必须有类别")
    @Min(value = 1, message = "活动类型取值范围1-5")
    @Max(value = 5, message = "活动类型取值范围1-5")
    private Integer activityType;

    /**
     * 开始时间
     */
    @NotNull(message = "活动开始时间必填")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Future(message = "必须是未来时间")
    @NotNull(message = "结束时间必填")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 活动地点
     */
    @NotBlank(message = "请填写正确地点")
    private String location;

    /**
     * 详细位置（用于定位）
     */
    @NotNull
    private String locationDetail;

    /**
     * 预计参与人数
     */
    @NotNull(message = "参与人数不为空")
    @Min(value = 11, message = "参与人数必须大于10")
    private Integer expectedPeople;

    /**
     * 预算金额
     */
    @PositiveOrZero(message = "预算金额不能为负")
    @NotNull(message = "请添加正确的预算金额")
    private BigDecimal budget;

    /**
     * 活动内容
     */
    @NotNull(message = "请填写正确的活动内容")
    private String activityContent;

    /**
     * 安全预案
     */
    @NotNull(message = "请正确填写活动预案")
    private String safetyPlan;

    /**
     * 申请人
     */
    @NotNull(message = "必须填写申请人姓名")
    private Long applyUserId;

    @NotNull(message = "申请时间必须填写")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 当前审批步骤
     */
    @NotNull
    private Integer currentApproveStep;

    /**
     * 1:草稿 2:待审批 3:审批中 4:已通过 5:已驳回 6:已取消
     */
    @NotNull
    private Integer approveStatus;

    /**
     * 驳回原因
     */
    @NotBlank
    private String rejectReason;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}