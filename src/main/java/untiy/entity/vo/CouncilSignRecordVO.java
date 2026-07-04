package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CouncilSignRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String roleCode;
    private Integer level;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime signTime;
}
