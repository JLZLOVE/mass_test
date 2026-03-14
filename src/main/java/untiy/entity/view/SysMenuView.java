
package untiy.entity.view;

import untiy.entity.SysMenu;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 菜单权限表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenuView extends SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysMenuView() {
    }

    public SysMenuView(SysMenu sysMenu) {
        BeanUtils.copyProperties(sysMenu, this);
    }
}