package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AssignRoleDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 数据范围类型：1=学院 2=社团 3=部门；data_scope 为全部/仅自己时须为空 */
    private Integer scopeType;

    /** 数据范围 ID（学院/社团/部门主键；全局范围须为空）—— 服务器内部使用，前端不传 */
    private Long scopeId;

    /** scope_type=2 时，前端传入社团名称，服务端内部转为 scopeId */
    private String clubName;
}
