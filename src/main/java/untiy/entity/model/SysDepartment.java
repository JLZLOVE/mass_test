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
 * 社团部门表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_department")
public class SysDepartment implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 所属社团
     */
    private Long clubId;

    /**
     * 父部门ID（用于部门层级）
     */
    private Long parentId;

    /**
     * 部长ID
     */
    private Long leaderId;

    /**
     * 部门职责
     */
    private String description;

    private Integer status;

    private LocalDateTime createTime;


}
