package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 社团部门表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysDepartmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
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
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
}