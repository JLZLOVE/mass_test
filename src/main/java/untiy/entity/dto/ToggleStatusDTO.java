package untiy.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ToggleStatusDTO {

    /** 目标用户名列表 */
    private List<String> usernames;

    /** 目标状态：0=禁用，1=启用 */
    private Integer status;
}
