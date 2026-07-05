package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import untiy.entity.NoticeInfo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private NoticeInfo notice;

    /** 是否加红标识 */
    private Boolean highlight;

    /** 当前用户可见附件（权限过滤后） */
    private List<String> visibleAttachments;

    /** 当前用户是否已读 */
    private Boolean read;

    /** 当前用户是否已确认 */
    private Boolean confirmed;

    /** 发布人查看：已读人数 */
    private Long readCount;

    /** 发布人查看：已确认人数 */
    private Long confirmCount;

    /** 发布人查看：接收总人数（固化或动态估算） */
    private Long receiverCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime queryTime;
}
