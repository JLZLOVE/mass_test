package untiy.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeSendDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "接收范围类型不能为空")
    private Integer receiverType;

    @NotBlank(message = "接收范围值不能为空")
    private String receiverValues;

    private Integer importance;

    private Integer urgency;

    @NotNull(message = "是否需要确认不能为空")
    private Boolean needConfirm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledPublishTime;

    private Boolean pinned;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pinExpireAt;

    @NotNull(message = "是否长期可见不能为空")
    private Boolean longTermVisible;

    private List<String> attachments;

    private Integer attachmentMinLevel;

    private Long templateId;

    /** true=保存草稿不发送 */
    private Boolean draft;
}
