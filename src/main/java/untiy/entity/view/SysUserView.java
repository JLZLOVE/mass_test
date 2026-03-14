
package untiy.entity.view;

import untiy.entity.SysUser;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 用户基础表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUserView extends SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysUserView() {
    }

    public SysUserView(SysUser sysUser) {
        BeanUtils.copyProperties(sysUser, this);
    }
}