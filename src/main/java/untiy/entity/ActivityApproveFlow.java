package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 活动审批流程表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)

public class ActivityApproveFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @NotNull(message = "活动编号不为空")
    private Long activityId;

    /**
     * 步骤序号
     */
    @NotNull(message = "活动序号不为空")
    private Integer step;

    /**
     * 审批角色ID
     */
    @NotNull(message = "审批人信息不为空")
    private Long approveRoleId;

    /**
     * 实际审批人ID
     */
    @NotNull(message = "审批人信息不为空")
    private Long approveUserId;

    /**
     * 1:通过 2:驳回
     */
    private Integer approveResult;

    /**
     * 审批意见
     */
    @NotBlank(message = "必须要审批意见")
    private String approveOpinion;

    /**
     * 审批时间
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
