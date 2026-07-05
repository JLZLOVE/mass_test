package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ActivityApproveDTO {

    @NotNull(message = "版本号不能为空")
    private Integer version;

    @NotBlank(message = "审批意见不能为空")
    private String opinion;

    /** 指导老师可选：调整活动级别 1院级 2校级 */
    private Integer activityLevel;
}
