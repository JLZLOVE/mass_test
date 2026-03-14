
package untiy.entity.view;

import untiy.entity.SysDataPermission;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 数据权限规则表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_data_permission")
public class SysDataPermissionView extends SysDataPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysDataPermissionView() {
    }

    public SysDataPermissionView(SysDataPermission sysDataPermission) {
        BeanUtils.copyProperties(sysDataPermission, this);
    }
}