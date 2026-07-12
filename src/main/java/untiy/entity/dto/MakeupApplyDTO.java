package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MakeupApplyDTO {

    @NotBlank(message = "补签用户不能为空")
    private String username;

    @NotNull(message = "原因类型不能为空")
    private Integer reasonType;

    @NotBlank(message = "说明不能为空")
    private String reasonDetail;

    private String attachment;
}
