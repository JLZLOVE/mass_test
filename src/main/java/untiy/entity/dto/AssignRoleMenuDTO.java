package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AssignRoleMenuDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 菜单 ID 列表；空列表表示清空该角色全部菜单绑定 */
    private List<Long> menuIds;
}
