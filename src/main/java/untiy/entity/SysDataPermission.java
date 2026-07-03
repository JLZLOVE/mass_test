package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * <p>
 * 数据权限规则表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysDataPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 表名
     */
    @NotBlank(message = "表名不能为空")
    private String tableName;

    /**
     * 字段名
     */
    @NotBlank(message = "字段名不能为空")
    private String fieldName;

    /**
     * 是否可见 0:隐藏 1:可见 2:只读
     */
    @Min(value = 0, message = "是否可见只能为0、1、2")
    @Max(value = 2, message = "是否可见只能为0、1、2")
    private Integer visible;          // 有默认值1，不加 @NotNull

    /**
     * 行级条件 1:全部 2:本部门 3:本人
     */
    @Min(value = 1, message = "行级条件只能为1、2、3")
    @Max(value = 3, message = "行级条件只能为1、2、3")
    private Integer conditionType;
    /**
     * 自定义条件
     */
    private String conditionValue;

    private String description;

    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;

}
