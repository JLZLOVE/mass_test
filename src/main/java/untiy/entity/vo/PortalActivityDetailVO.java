package untiy.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 门户 - 活动详情 VO（继承列表全部字段）
 *
 * @author 玖
 * @since 2026-07-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PortalActivityDetailVO extends PortalActivityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 活动完整内容 */
    private String content;

    /** 主办/承办/指派说明 */
    private String organizerNote;

    /** 当前是否在签到窗口内 */
    private Boolean signAvailable;

    /** 签到开始时间 */
    private String signStartTime;

    /** 签到结束时间 */
    private String signEndTime;

    /** 签到方式（1定位/2扫码/3两者） */
    private Integer signMode;

    /** 是否启用签退 */
    private Boolean checkoutEnabled;
}