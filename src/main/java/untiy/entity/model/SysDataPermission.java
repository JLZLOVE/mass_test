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
 * 数据权限规则表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_data_permission")
public class SysDataPermission implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 是否可见 0:隐藏 1:可见 2:只读
     */
    private Integer visible;

    /**
     * 行级条件 1:全部 2:本部门 3:本人
     */
    private Integer conditionType;

    /**
     * 自定义条件
     */
    private String conditionValue;

    private String description;

    private Integer status;

    private LocalDateTime createTime;


}
