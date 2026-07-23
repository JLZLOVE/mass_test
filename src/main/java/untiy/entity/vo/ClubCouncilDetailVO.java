package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 合议签字详情。
 */
@Data
public class ClubCouncilDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long clubId;

    private String clubCode;

    private String clubName;

    private Long collegeId;

    private String collegeName;

    private String reason;

    /** 1:合议中 2:已通过 3:已驳回 */
    private Integer status;

    private String initiatorName;

    private List<CouncilSignRecordVO> signatories = new ArrayList<>();

    /** 当前用户是否已签字 */
    private Boolean alreadySigned;

    /** 当前用户是否可签字 */
    private Boolean canSign;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime executedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
