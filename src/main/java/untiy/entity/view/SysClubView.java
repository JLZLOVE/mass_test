
package untiy.entity.view;

import untiy.entity.SysClub;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 社团表 View
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_club")
public class SysClubView extends SysClub implements Serializable {

    private static final long serialVersionUID = 1L;

    public SysClubView() {
    }

    public SysClubView(SysClub sysClub) {
        BeanUtils.copyProperties(sysClub, this);
    }
}