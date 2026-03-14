package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 菜单权限表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 父菜单ID
             */
            private Long parentId;
            /**
             * 菜单名称
             */
            private String menuName;
            /**
             * 类型 1:目录 2:菜单 3:按钮
             */
            private Integer menuType;
            /**
             * 权限标识（如 user:list, activity:approve）
             */
            private String permissionCode;
            /**
             * 前端组件路径
             */
            private String componentPath;
            /**
             * 路由路径
             */
            private String routePath;
            /**
             * 图标
             */
            private String icon;
            /**
             * 排序
             */
            private Integer sort;
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
}