package untiy.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;

/**
 * <p>
 * 社团统计表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ClubStatistics implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @NotNull(message = "社团编号不可为空")
    private Long clubId;

    /**
     * 统计日期
     */

    private LocalDate statDate;

    /**
     * 当前总成员数
     */
    // 移除冲突的 @PositiveOrZero，保留 @NotNull、@Min、@Max
    @NotNull(message = "社团成员必须填写")
    @Min(value = 3,message = "社团成员最少3人")
    @Max(value = 300,message = "社团成员最大不超过300")
    private Integer totalMembers;

    /**
     * 新增成员数
     */
    // 移除 @PositiveOrZero，@Max 修正为 200（与 message 一致）
    @NotNull(message = "新增不得为空")
    @Min(value = 1,message = "新增最少为1人,最多不超过200人")
    @Max(value = 200,message = "新增最少为1人,最多不超过200人")
    private Integer newMembers;

    /**
     * 活动次数
     */
    // @Positive 改为 @PositiveOrZero，允许 0
    @PositiveOrZero(message = "社团活动次数至少为零")
    @NotNull(message = "活动次数无时填写零")
    private Integer activityCount;

    /**
     * 参与人次
     */
    // 移除 @PositiveOrZero，@Min 修正为 10（与 message 一致）
    @NotNull(message = "活动人数不得为空")
    @Min(value = 10,message = "活动人数最少为10人人,最多不超过5000人")
    @Max(value = 5000,message = "活动人数最少为10人人,最多不超过5000人")
    private Integer totalParticipants;

    /**
     * 总预算
     */
    // @Positive 改为 @PositiveOrZero，@Min 修正为 0（与 message 一致）
    @PositiveOrZero(message = "活动预算不为负")
    @NotNull(message = "必须填写活动预算")
    @Min(value = 0,message = "活动预算最少为0,最多不超过3万元")
    @Max(value = 30000,message = "活动预算最少为0,最多不超过3万元")
    private BigDecimal totalBudget;

    /**
     * 活动平均评分
     */

    private BigDecimal avgScore;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}