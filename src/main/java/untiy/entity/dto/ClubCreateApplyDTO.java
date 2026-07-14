package untiy.entity.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import untiy.entity.dto.deser.UsernameStringDeserializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(name = "ClubCreateApplyDTO", description = "社团创建申请")
public class ClubCreateApplyDTO {

    @NotBlank(message = "社团名称不能为空")
    private String clubName;

    @NotNull(message = "挂靠学院不能为空")
    private Long collegeId;

    @NotBlank(message = "社团类别不能为空")
    @Schema(description = "社团性质，须为 ClubCategory 六类之一")
    private String category;

    private String description;

    @NotBlank(message = "拟定社长学号/工号不能为空")
    @Schema(description = "拟定社长 username（学号/工号），非数据库 id")
    @JsonDeserialize(using = UsernameStringDeserializer.class)
    private String proposedLeaderUsername;

    @NotNull(message = "最大人数不能为空")
    private Integer maxMembers;
}
