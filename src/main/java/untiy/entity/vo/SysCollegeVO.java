package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 学院表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysCollegeVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 学院名称
             */
            private String collegeName;
            /**
             * 学院代码
             */
            private String collegeCode;
            /**
             * 院长ID（关联sys_user）
             */
            private Long deanId;
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
}