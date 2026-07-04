package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ClubCreateApplyDTO {

    @NotBlank(message = "社团名称不能为空")
    private String clubName;

    @NotNull(message = "挂靠学院不能为空")
    private Long collegeId;

    @NotBlank(message = "社团类别不能为空")
    private String category;

    private String description;

    @NotNull(message = "拟定社长不能为空")
    private Long proposedLeaderId;

    @NotNull(message = "最大人数不能为空")
    private Integer maxMembers;
}
