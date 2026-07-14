package untiy.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClubCollegeApproveDTO extends ClubApproveDTO {

    /** 申请人 username（学号/工号）；与 applicationNo 二选一，applicationNo 优先 */
    private String username;

    /** 申请编号 */
    private String applicationNo;
}
