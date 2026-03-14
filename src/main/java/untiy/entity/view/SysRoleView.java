
package untiy.entity.view;

import untiy.entity.SysRole;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 角色表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRoleView extends SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysRoleView() {
    }

    public SysRoleView(SysRole sysRole) {
        BeanUtils.copyProperties(sysRole, this);
    }
}