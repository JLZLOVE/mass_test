package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端社团列表 / 详情 VO（对外暴露 id，供成员数聚合与合议跳转）。
 */
@Data
public class SysClubListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String clubCode;

    private String clubName;

    /** 中文类别标签 */
    private String category;

    /** 类别前缀码 SZ/XS/CX/WH/GY/ZL */
    private String categoryCode;

    private Long collegeId;

    private String collegeName;

    private Long advisorId;

    /** 指导老师姓名；空则前端展示「待指派」 */
    private String advisorName;

    /** 0:解散 1:正常 */
    private Integer status;

    private String description;

    private String logo;

    /** 进行中的解散申请编号（申请解散中 Tab） */
    private String dissolveApplicationNo;

    /** 进行中的合议 ID（合议中 Tab / 签字跳转） */
    private Long activeCouncilId;

    /** 后端下发：当前用户是否可对本社团发起解散 */
    private Boolean canDissolve;

    /** 后端下发：当前用户是否可进入合议签字 */
    private Boolean canSignCouncil;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime dissolveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
