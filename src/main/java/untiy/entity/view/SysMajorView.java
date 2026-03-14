
package untiy.entity.view;

import untiy.entity.SysMajor;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 专业表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_major")
public class SysMajorView extends SysMajor implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysMajorView() {
    }

    public SysMajorView(SysMajor sysMajor) {
        BeanUtils.copyProperties(sysMajor, this);
    }
}