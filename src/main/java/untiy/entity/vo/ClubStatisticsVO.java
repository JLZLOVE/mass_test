package untiy.entity.vo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 社团统计表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class ClubStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 
             */
            private Long clubId;
            /**
             * 统计日期
             */
            private LocalDate statDate;
            /**
             * 当前总成员数
             */
            private Integer totalMembers;
            /**
             * 新增成员数
             */
            private Integer newMembers;
            /**
             * 活动次数
             */
            private Integer activityCount;
            /**
             * 参与人次
             */
            private Integer totalParticipants;
            /**
             * 总预算
             */
            private BigDecimal totalBudget;
            /**
             * 活动平均评分
             */
            private BigDecimal avgScore;
            /**
             * 
             */
            private LocalDateTime createTime;
}