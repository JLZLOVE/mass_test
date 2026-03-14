
package untiy.entity.view;

import untiy.entity.NoticeCategory;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 通知分类表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice_category")
public class NoticeCategoryView extends NoticeCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    public NoticeCategoryView() {
    }

    public NoticeCategoryView(NoticeCategory noticeCategory) {
        BeanUtils.copyProperties(noticeCategory, this);
    }
}