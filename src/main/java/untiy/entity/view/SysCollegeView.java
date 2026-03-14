
package untiy.entity.view;

import untiy.entity.SysCollege;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 学院表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_college")
public class SysCollegeView extends SysCollege implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysCollegeView() {
    }

    public SysCollegeView(SysCollege sysCollege) {
        BeanUtils.copyProperties(sysCollege, this);
    }
}