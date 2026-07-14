package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CouncilInitiateDTO {

    @NotBlank(message = "社团编号不能为空")
    private String clubCode;

    @NotBlank(message = "解散原因不能为空")
    private String reason;
}
