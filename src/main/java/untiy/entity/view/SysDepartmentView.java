
package untiy.entity.view;

import untiy.entity.SysDepartment;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 社团部门表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartmentView extends SysDepartment implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysDepartmentView() {
    }

    public SysDepartmentView(SysDepartment sysDepartment) {
        BeanUtils.copyProperties(sysDepartment, this);
    }
}