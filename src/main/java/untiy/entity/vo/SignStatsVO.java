package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SignStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long activityId;

    private Integer expectedCount;

    private Long signedCount;

    private Integer unsignedCount;

    private BigDecimal signRate;

    private Long lateCount;

    private Long earlyLeaveCount;

    /** 每半小时签到人数，key 如 "14:00-14:30" */
    private Map<String, Long> timeDistribution;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime queryTime;
}
