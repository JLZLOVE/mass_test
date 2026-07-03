package untiy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 活动分类表
 * </p>
 *
 * @author 玖
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ActivityCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类信息不可为空")
    private String categoryName;

    /**
     * 审批流程模板ID
     */
    @NotNull(message = "审批id不得为空")
    private Long approveFlowId;

    /**
     * 是否需要定位签到
     */
    @NotNull
    private Integer needLocation;

    /**
     * 是否需要活动报告
     */
    @NotNull
    private Integer needReport;
    @NotNull(message = "状态不得为空")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
//    编码后缀类型

    @NotNull
    @TableField(exist = false)
    private String codeSuffix;

}
