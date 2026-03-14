package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 用户角色关联表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysUserRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 用户ID
             */
            private Long userId;
            /**
             * 角色ID
             */
            private Long roleId;
            /**
             * 范围类型 1:学院 2:社团 3:部门 4:专业 5:班级
             */
            private Integer scopeType;
            /**
             * 具体范围ID
             */
            private Long scopeId;
            /**
             * 
             */
            private LocalDateTime createTime;
}