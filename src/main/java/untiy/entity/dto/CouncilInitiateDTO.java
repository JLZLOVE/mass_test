package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CouncilInitiateDTO {

    @NotNull(message = "社团ID不能为空")
    private Long clubId;

    @NotBlank(message = "解散原因不能为空")
    private String reason;
}
