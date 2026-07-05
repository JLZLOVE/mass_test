package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import untiy.entity.ActivityApply;
import untiy.entity.ActivityApplyHistory;
import untiy.entity.ActivityApproveFlow;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityApplyDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ActivityApply apply;

    private List<ActivityApproveFlow> flows;

    private List<ActivityApplyHistory> histories;

    private String currentApproverName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime queryTime;
}
