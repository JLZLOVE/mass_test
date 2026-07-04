package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AssignRoleDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 数据范围类型（2=社团，3=部门；其他角色须为空） */
    private Integer scopeType;

    /** 数据范围ID（可空） */
    private Long scopeId;
}
