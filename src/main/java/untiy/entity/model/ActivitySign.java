package untiy.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动签到表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("activity_sign")
public class ActivitySign implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    /**
     * 1:自动定位 2:手动签到 3:补签
     */
    private Integer signType;

    private LocalDateTime signTime;

    /**
     * 签到经纬度
     */
    private Integer signLocation;

    /**
     * 签到地址文本
     */
    private String address;

    /**
     * 1:正常 2:迟到 3:早退
     */
    private Integer signStatus;

    private LocalDateTime createTime;


}
