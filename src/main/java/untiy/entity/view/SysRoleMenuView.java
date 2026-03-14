
package untiy.entity.view;

import untiy.entity.SysRoleMenu;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 角色菜单关联表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenuView extends SysRoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysRoleMenuView() {
    }

    public SysRoleMenuView(SysRoleMenu sysRoleMenu) {
        BeanUtils.copyProperties(sysRoleMenu, this);
    }
}