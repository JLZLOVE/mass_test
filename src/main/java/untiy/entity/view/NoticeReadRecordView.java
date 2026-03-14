
package untiy.entity.view;

import untiy.entity.NoticeReadRecord;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 通知阅读记录表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice_read_record")
public class NoticeReadRecordView extends NoticeReadRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    public NoticeReadRecordView() {
    }

    public NoticeReadRecordView(NoticeReadRecord noticeReadRecord) {
        BeanUtils.copyProperties(noticeReadRecord, this);
    }
}