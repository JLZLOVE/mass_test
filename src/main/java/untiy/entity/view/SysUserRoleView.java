
package untiy.entity.view;

import untiy.entity.SysUserRole;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 用户角色关联表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_role")
public class SysUserRoleView extends SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysUserRoleView() {
    }

    public SysUserRoleView(SysUserRole sysUserRole) {
        BeanUtils.copyProperties(sysUserRole, this);
    }
}