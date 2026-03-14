package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 专业表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysMajorVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
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
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
}