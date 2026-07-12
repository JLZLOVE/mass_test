package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notice_info")
public class NoticeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Long categoryId;

    @JsonIgnore
    private Long publisherId;

    private String publisherName;

    /** 重要程度 1高 2中 3低 */
    private Integer importance;

    /** 紧急程度 1紧急 2不紧急 */
    private Integer urgency;

    /** 接收类型 1全体学生 2全体老师 3指定角色 4指定社团 5指定人员 */
    private Integer receiverType;

    /** JSON：角色ID/社团ID/用户ID列表 */
    private String receiverValues;

    private Integer needConfirm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledPublishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /** 0草稿 1已发布 2已撤回 */
    private Integer status;

    private Integer isPinned;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pinExpireAt;

    /** 1长期可见 0仅当前成员 */
    private Integer longTermVisible;

    /** JSON 附件路径列表 */
    private String attachments;

    /** 附件最低等级 0-4 */
    private Integer attachmentMinLevel;

    /** 0不可撤回 */
    private Integer revocable;

    /** 0手动 1活动取消自动 */
    private Integer sourceType;

    private Long templateId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
