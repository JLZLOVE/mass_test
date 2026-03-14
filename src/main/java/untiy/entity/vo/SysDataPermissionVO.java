package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 数据权限规则表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class SysDataPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 角色ID
             */
            private Long roleId;
            /**
             * 表名
             */
            private String tableName;
            /**
             * 字段名
             */
            private String fieldName;
            /**
             * 是否可见 0:隐藏 1:可见 2:只读
             */
            private Integer visible;
            /**
             * 行级条件 1:全部 2:本部门 3:本人
             */
            private Integer conditionType;
            /**
             * 自定义条件
             */
            private String conditionValue;
            /**
             * 
             */
            private String description;
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
}