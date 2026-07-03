package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 直属菜单ID
     */
    private Long parentId;          // 允许为空或有默认值0，不加非空校验

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 类型 1:目录 2:菜单 3:按钮
     */
    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型只能为1、2、3")
    @Max(value = 3, message = "菜单类型只能为1、2、3")
    private Integer menuType;

    /**
     * 权限标识（如 user:list, activity:approve）
     */
    private String permissionCode;  // 允许为空，不加校验

    /**
     * 前端组件路径
     */
    private String componentPath;   // 允许为空，不加校验

    /**
     * 路由路径
     */
    private String routePath;       // 允许为空，不加校验

    /**
     * 图标
     */
    private String icon;           // 允许为空，不加校验

    /**
     * 排序
     */
    @Min(value = 0, message = "排序值不能为负数")
    private Integer sort;          // 有默认值0，不加 @NotNull，仅约束范围

    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;       // 有默认值1，不加 @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime; // 自动填充，无校验

}
