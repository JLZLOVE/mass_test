package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SignRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonIgnore
    private Long userId;

    private String realName;

    private String username;

    private Integer signType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime signTime;

    private String address;

    private Integer isLate;

    private Integer isEarlyLeave;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkoutTime;
}
