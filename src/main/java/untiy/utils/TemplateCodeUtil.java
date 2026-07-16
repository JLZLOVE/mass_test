package untiy.utils;

import org.apache.commons.lang3.StringUtils;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * templateCode 生成与防篡改校验。
 * <p>
 * 格式：{业务前缀}_{yyyyMMddHHmm}_{6位随机数字}，例 CLUB_202607161430_827391
 */
public final class TemplateCodeUtil {

    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]*_\\d{12}_\\d{6}$");

    private TemplateCodeUtil() {
    }

    public static String generate(String businessPrefix, LocalDateTime createTime) {
        if (StringUtils.isBlank(businessPrefix)) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        LocalDateTime minute = truncateToMinute(createTime != null ? createTime : LocalDateTime.now());
        int random = ThreadLocalRandom.current().nextInt(1_000_000);
        return businessPrefix.trim().toUpperCase() + "_"
                + minute.format(MINUTE_FORMAT) + "_"
                + String.format("%06d", random);
    }

    /** 解析编码中的分钟级时间串，并与 createTime 截断到分钟后比对 */
    public static void assertMatchesCreateTime(String templateCode, LocalDateTime createTime) {
        if (StringUtils.isBlank(templateCode)) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        if (createTime == null) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_TAMPER_CODE, ErrorConfig.TEMPLATE_CODE_TAMPER_MSG);
        }
        String encodedMinute = extractMinutePart(templateCode);
        String expectedMinute = truncateToMinute(createTime).format(MINUTE_FORMAT);
        if (!expectedMinute.equals(encodedMinute)) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_TAMPER_CODE, ErrorConfig.TEMPLATE_CODE_TAMPER_MSG);
        }
    }

    public static String extractMinutePart(String templateCode) {
        if (!CODE_PATTERN.matcher(templateCode).matches()) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        return templateCode.split("_", 3)[1];
    }

    public static LocalDateTime truncateToMinute(LocalDateTime time) {
        return time.withSecond(0).withNano(0);
    }
}
