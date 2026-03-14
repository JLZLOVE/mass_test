package untiy.entity;

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
 * 社团表
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_club")
public class SysClub implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 社团名称
     */
    private String clubName;

    /**
     * 社团编号
     */
    private String clubCode;

    /**
     * 社团类别（学术科技/文化体育/公益等）
     */
    private String category;

    /**
     * 挂靠学院（可为空）
     */
    private Long collegeId;

    /**
     * 指导老师ID
     */
    private Long advisorId;

    /**
     * 社团简介
     */
    private String description;

    /**
     * 社团logo
     */
    private String logo;

    /**
     * 状态 0:解散 1:正常
     */
    private Integer status;

    private LocalDateTime createTime;


}
