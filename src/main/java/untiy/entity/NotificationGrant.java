package untiy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 通知授权表
 * </p>
 *
 * @author 玖
 * @since 2026-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("notification_grant")
public class NotificationGrant implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 授权人ID（社长/指导老师/书记）
     */
    private Long grantorId;

    /**
     * 被授权人ID
     */
    private Long granteeId;

    /**
     * 1:发全社通知 2:发跨社团通知 3:发跨学院通知
     */
    private Integer grantType;

    /**
     * 1:社团 2:学院（授权范围类型）
     */
    private Integer scopeType;

    /**
     * 社团ID或学院ID
     */
    private Long scopeId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime grantedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireAt;

    /**
     * 是否已撤销
     */
    private Boolean revoked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime revokedAt;

    /**
     * 撤销人ID
     */
    private Long revokedBy;


}