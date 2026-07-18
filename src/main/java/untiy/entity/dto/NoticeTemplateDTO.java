package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NoticeTemplateDTO {

    /**
     * 模板编码（创建后由服务端生成并返回；更新时必填，作为唯一标识）。
     * 格式：{社团6类前缀}{yyyyMMddHHmm}{5位随机}（19位），如 WH20260718143082739
     */
    private String templateName;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long categoryId;
}
