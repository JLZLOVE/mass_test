package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("activity_apply")
public class ActivityApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String activityNo;

    private Long clubId;

    private String activityName;

    private Long categoryId;

    /** 1:日常活动 2:比赛 3:演出 4:讲座 5:其他 */
    private Integer activityType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String location;

    private String locationDetail;

    private Integer expectedPeople;

    private BigDecimal budget;

    private String activityContent;

    /** 活动封面图路径 */
    private String coverImage;

    /** 主办/承办/指派说明 */
    private String organizerNote;

    private String safetyPlan;

    /** 申请附件（本地路径） */
    private String attachment;

    @JsonIgnore
    private Long applyUserId;

    @TableField(exist = false)
    private String applyUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    private Integer currentApproveStep;

    /** 1草稿 2待审批 3审批中 4已通过 5已驳回 6已取消 7变更审批中 */
    private Integer approveStatus;

    /** 1院级 2校级 */
    private Integer activityLevel;

    /** 指导老师调整级别后锁定 */
    private Integer levelAdjustLocked;

    @Version
    private Integer version;

    private String rejectReason;

    private String summaryContent;

    private String summaryAttachment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime summaryUploadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
