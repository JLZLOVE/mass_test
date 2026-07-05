package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ActivityCancelDTO {

    @NotNull(message = "版本号不能为空")
    private Integer version;

    @NotBlank(message = "取消原因不能为空")
    private String reason;
}
