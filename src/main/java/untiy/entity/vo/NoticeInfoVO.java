package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 通知表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class NoticeInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 标题
             */
            private String title;
            /**
             * 内容（富文本）
             */
            private String content;
            /**
             * 分类
             */
            private Long categoryId;
            /**
             * 发布人ID
             */
            private Long publisherId;
            /**
             * 发布人姓名（冗余）
             */
            private String publisherName;
            /**
             * 重要程度 1:低 2:中 3:高
             */
            private Integer importance;
            /**
             * 紧急程度 1:不紧急 2:紧急
             */
            private Integer urgency;
            /**
             * 接收类型 1:全体学生 2:全体老师 3:指定角色 4:指定社团 5:指定人员
             */
            private Integer receiverType;
            /**
             * 接收者值（角色ID/社团ID/用户ID列表）
             */
            private String receiverValues;
            /**
             * 是否需要确认阅读
             */
            private Integer needConfirm;
            /**
             * 发布时间
             */
            private LocalDateTime publishTime;
            /**
             * 过期时间
             */
            private LocalDateTime expireTime;
            /**
             * 0:草稿 1:已发布 2:已撤回
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
            /**
             * 
             */
            private LocalDateTime updateTime;
}