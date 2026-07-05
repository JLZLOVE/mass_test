package untiy.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivitySubmitDTO {

    @NotNull(message = "社团不能为空")
    private Long clubId;

    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    @NotNull(message = "活动分类不能为空")
    private Long categoryId;

    @NotNull(message = "活动类型不能为空")
    @Min(value = 1, message = "活动类型无效")
    private Integer activityType;

    @NotNull(message = "活动级别不能为空")
    private Integer activityLevel;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "活动地点不能为空")
    private String location;

    private String locationDetail;

    @NotNull(message = "预计人数不能为空")
    @Min(value = 1, message = "预计人数无效")
    private Integer expectedPeople;

    @NotNull(message = "预算不能为空")
    private BigDecimal budget;

    @NotBlank(message = "活动内容不能为空")
    private String activityContent;

    @NotBlank(message = "安全预案不能为空")
    private String safetyPlan;

    /** 附件路径（先调用上传接口获得） */
    private String attachment;
}
