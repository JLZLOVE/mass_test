package untiy.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码（如：CLUB_PRESIDENT）
     */
    private String roleCode;

    /**
     * 角色等级（数字越小权限越高）
     */
    private Integer roleLevel;

    /**
     * 默认数据范围 1:全部 2:本学院 3:本社团 4:本部门 5:仅自己
     */
    private Integer dataScope;

    private String description;

    private Integer status;

    private LocalDateTime createTime;


}
