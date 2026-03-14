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
 * 专业表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysMajor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专业名称
     */
    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    /**
     * 专业代码
     */
    @NotBlank(message = "专业代码必须要写")
    private String majorCode;       // 表允许为空，不加非空校验

    /**
     * 所属学院
     */
    @NotNull(message = "所属学院不能为空")
    private Long collegeId;

    /**
     * 专业负责人ID
     */
    @NotNull(message = "负责人id不为空,如若错误,后续任何申请将不会通过")
    private Long headTeacherId;    // 表允许为空，不加非空校验

    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;        // 有默认值1，不加 @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 自动填充，无校验

}