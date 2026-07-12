package untiy.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SignConfigDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "签到方式不能为空")
    private Integer signMode;

    @NotNull(message = "签到开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signStartTime;

    @NotNull(message = "签到结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signEndTime;

    private Integer signRadius;

    @NotNull(message = "签退开关不能为空")
    private Boolean enableCheckout;

    private BigDecimal centerLatitude;

    private BigDecimal centerLongitude;
}
