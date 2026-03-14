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
 * 通知表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NoticeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容（富文本）
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 分类
     */
    private Long categoryId;          // 可为空，不加校验

    /**
     * 发布人ID
     */
    @NotNull(message = "发布人ID不能为空")
    private Long publisherId;

    /**
     * 发布人姓名（冗余）
     */
    private String publisherName;     // 冗余字段，允许为空，不加校验

    /**
     * 重要程度 1:低 2:中 3:高
     */
    @Min(value = 1, message = "重要程度只能为1、2、3")
    @Max(value = 3, message = "重要程度只能为1、2、3")
    private Integer importance;       // 有默认值1，不加 @NotNull

    /**
     * 紧急程度 1:不紧急 2:紧急
     */
    @Min(value = 1, message = "紧急程度只能为1或2")
    @Max(value = 2, message = "紧急程度只能为1或2")
    private Integer urgency;          // 有默认值1，不加 @NotNull

    /**
     * 接收类型 1:全体学生 2:全体老师 3:指定角色 4:指定社团 5:指定人员
     */
    @NotNull(message = "接收类型不能为空")
    @Min(value = 1, message = "接收类型取值范围1-5")
    @Max(value = 5, message = "接收类型取值范围1-5")
    private Integer receiverType;

    /**
     * 接收者值（角色ID/社团ID/用户ID列表）
     */
    private String receiverValues;    // 复杂业务校验在服务层，此处不加

    /**
     * 是否需要确认阅读
     */
    @Min(value = 0, message = "是否需要确认阅读只能为0或1")
    @Max(value = 1, message = "是否需要确认阅读只能为0或1")
    private Integer needConfirm;      // 有默认值0，不加 @NotNull

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime; // 可为空，不加校验

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;  // 可为空，不加校验

    /**
     * 0:草稿 1:已发布 2:已撤回
     */
    @Min(value = 0, message = "状态只能为0、1、2")
    @Max(value = 2, message = "状态只能为0、1、2")
    private Integer status;           // 有默认值0，不加 @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 自动填充，无校验
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 自动填充，无校验

}