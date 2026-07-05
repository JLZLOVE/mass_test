package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NoticeTemplateDTO {

    private Long id;

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long categoryId;
}
