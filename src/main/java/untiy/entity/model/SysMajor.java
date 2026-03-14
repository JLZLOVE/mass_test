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
 * 专业表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_major")
public class SysMajor implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 所属学院
     */
    private Long collegeId;

    /**
     * 专业负责人ID
     */
    private Long headTeacherId;

    private Integer status;

    private LocalDateTime createTime;


}
