package untiy.entity.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 门户 - 社团风采 VO
 *
 * @author 玖
 * @since 2026-07-16
 */
@Data
public class PortalClubVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 社团名称 */
    private String clubName;

    /** 社团类别中文名 */
    private String category;

    /** 社团简介 */
    private String description;

    /** 社团 Logo 路径 */
    private String logo;

    /** 挂靠学院名称 */
    private String collegeName;
}