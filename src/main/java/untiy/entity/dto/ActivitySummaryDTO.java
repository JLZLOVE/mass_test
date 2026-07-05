package untiy.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ActivitySummaryDTO {

    @NotNull(message = "版本号不能为空")
    private Integer version;

    private String summaryContent;

    /** 总结附件路径（先调用上传接口获得） */
    private String summaryAttachment;
}
