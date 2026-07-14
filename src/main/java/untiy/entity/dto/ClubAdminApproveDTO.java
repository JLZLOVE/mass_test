package untiy.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClubAdminApproveDTO extends ClubApproveDTO {

    /** 申请人 username（学号/工号），对应 sys_user.username */
    @NotBlank(message = "username 不能为空")
    private String username;
}
