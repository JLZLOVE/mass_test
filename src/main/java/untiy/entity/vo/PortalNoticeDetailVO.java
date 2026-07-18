package untiy.entity.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 门户 - 通知详情 VO
 *
 * @author 玖
 * @since 2026-07-16
 */
@Data
public class PortalNoticeDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知编号 */
    private String noticeNo;

    /** 标题 */
    private String title;

    /** 完整正文 */
    private String content;

    /** 发布时间 */
    private String publishTime;

    /** 是否置顶 */
    private Boolean topFlag;

    /** 题图路径 */
    private String coverImage;

    /** 附件列表（仅公开附件） */
    private List<String> attachments;

    /** 已读人数 */
    private Integer readCount;

    /** 接收总人数 */
    private Integer receiverCount;

    /** 阅读率 */
    private String readRate;

    /** 浏览量 */
    private Integer viewCount;
}