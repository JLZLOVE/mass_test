package untiy.entity.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 门户 - 活动列表 VO
 *
 * @author 玖
 * @since 2026-07-16
 */
@Data
public class PortalActivityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 活动编号（对外标识） */
    private String activityNo;

    /** 活动标题 */
    private String activityName;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 活动地点 */
    private String location;

    /** 主办社团名称 */
    private String clubName;

    /** 主办社团类别中文名 */
    private String clubCategoryName;

    /** 活动级别（1院级/2校级） */
    private Integer activityLevel;

    /** 活动分类名称 */
    private String categoryName;

    /** 封面图路径 */
    private String coverImage;
}