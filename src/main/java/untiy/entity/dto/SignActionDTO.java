package untiy.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SignActionDTO {

    /** 1定位 2扫码 */
    private Integer signMethod;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String address;

    /** 扫码签到令牌 */
    private String qrToken;
}
