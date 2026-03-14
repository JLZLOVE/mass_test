package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 通知阅读记录表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class NoticeReadRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 
             */
            private Long noticeId;
            /**
             * 
             */
            private Long userId;
            /**
             * 阅读时间
             */
            private LocalDateTime readTime;
            /**
             * 确认时间（如果need_confirm=1）
             */
            private LocalDateTime confirmTime;
            /**
             * 是否已确认
             */
            private Integer isConfirmed;
}