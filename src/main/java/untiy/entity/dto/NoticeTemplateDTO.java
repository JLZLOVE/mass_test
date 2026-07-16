package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NoticeTemplateDTO {

    /**
     * 模板编码（创建后由服务端生成并返回；更新时必填，作为唯一标识）。
     * 格式：{前缀}_{yyyyMMddHHmm}_{6位随机数}，如 NOTICE_202607161430_827391
     */
    private String templateName;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long categoryId;
}
