
package untiy.entity.view;

import untiy.entity.NoticeInfo;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 通知表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice_info")
public class NoticeInfoView extends NoticeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public NoticeInfoView() {
    }

    public NoticeInfoView(NoticeInfo noticeInfo) {
        BeanUtils.copyProperties(noticeInfo, this);
    }
}