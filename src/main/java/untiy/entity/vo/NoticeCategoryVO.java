package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 通知分类表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class NoticeCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 分类名称
             */
            private String categoryName;
            /**
             * 默认优先级
             */
            private Integer priority;
            /**
             * 
             */
            private String icon;
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
}