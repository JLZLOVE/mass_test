package untiy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 社团申请表
 * </p>
 *
 * @author 玖
 * @since 2026-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("club_application")
public class ClubApplication implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请编号
     */
    private String applicationNo;

    /**
     * 1:创建 2:解散
     */
    private Integer applyType;

    /**
     * 社团名称（创建时必填）
     */
    private String clubName;

    /**
     * 挂靠学院
     */
    private Long collegeId;

    /**
     * 社团类别
     */
    private String category;

    /**
     * 社团简介
     */
    private String description;

    /**
     * 拟定社长ID
     */
    private Long proposedLeaderId;

    /**
     * 最大招募人数
     */
    private Integer maxMembers;

    /**
     * 解散原因
     */
    private String dissolveReason;

    /**
     * 申请人ID（指导老师）
     */
    private Long applicantId;

    /**
     * 申请人名称
     */
    private String applicantName;

    /**
     * 1:待学院审批 2:学院已通过 3:已通过 4:已驳回 5:已撤回
     */
    private Integer status;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 学院审批人ID
     */
    private Long collegeApproverId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime collegeApproveTime;

    /**
     * 学院审批意见
     */
    private String collegeApproveOpinion;

    /**
     * 校级审批人ID
     */
    private Long adminApproverId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime adminApproveTime;

    /**
     * 校级审批意见
     */
    private String adminApproveOpinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


}