package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdminSignDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String address;
}
