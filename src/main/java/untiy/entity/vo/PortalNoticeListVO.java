package untiy.entity.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 门户 - 通知列表 VO
 *
 * @author 玖
 * @since 2026-07-16
 */
@Data
public class PortalNoticeListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知编号（对外标识） */
    private String noticeNo;

    /** 标题 */
    private String title;

    /** 内容摘要（运行时截取前150字符） */
    private String summary;

    /** 发布时间 */
    private String publishTime;

    /** 是否置顶 */
    private Boolean topFlag;

    /** 题图路径 */
    private String coverImage;

    /** 是否有公开附件 */
    private Boolean hasAttachment;

    /** 已读人数 */
    private Integer readCount;

    /** 接收总人数 */
    private Integer receiverCount;

    /** 阅读率（readCount / receiverCount × 100%） */
    private String readRate;

    /** 浏览量 */
    private Integer viewCount;
}