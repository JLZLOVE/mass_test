package untiy.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.io.Serializable;
import lombok.Data;

/**
 * 活动分类表 视图对象
 *
 * @author 玖
 * @since 2026-02-19
 */
@Data
public class ActivityCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

                /**
             * 
             */
            private Long id;
            /**
             * 分类名称
             */
            private String categoryName;
            /**
             * 审批流程模板ID
             */
            private Long approveFlowId;
            /**
             * 是否需要定位签到
             */
            private Integer needLocation;
            /**
             * 是否需要活动报告
             */
            private Integer needReport;
            /**
             * 
             */
            private Integer status;
            /**
             * 
             */
            private LocalDateTime createTime;
        /**
         *
         */
        private String codeSuffix;
}