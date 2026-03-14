package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 角色菜单关联表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysRoleMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 
             */
            private Long roleId;
            /**
             * 
             */
            private Long menuId;
            /**
             * 
             */
            private LocalDateTime createTime;
}