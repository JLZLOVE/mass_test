package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysUserRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Long userId;

    private String username;
    private String realName;

    @JsonIgnore
    private Long roleId;

    private String roleName;
    private String roleCode;
    private Integer scopeType;

    @JsonIgnore
    private Long scopeId;

    /** scope_type=2 时对应的社团名称 */
    private String clubName;

    /** scope_type=1 时对应的学院名称 */
    private String collegeName;

    /** scope_type=3 时对应的部门名称 */
    private String departmentName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
