package untiy.utils;

import org.apache.commons.lang3.StringUtils;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 统一编号生成与防篡改校验（模板编号、通知编号复用）。
 * <p>
 * 统一格式：{社团6类前缀}{yyyyMMddHHmm}{5位随机}（19位），例 WH20260718143082739
 */
public final class TemplateCodeUtil {

    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /** 格式：{2位前缀}{12位时间}{5位随机} = 19 位，前缀为社团6类缩写（SZ/XS/CX/WH/GY/ZL） */
    private static final Pattern CODE_PATTERN = Pattern.compile("^(SZ|XS|CX|WH|GY|ZL)\\d{12}\\d{5}$");

    private TemplateCodeUtil() {
    }

    /**
     * 生成统一格式编号：{prefix}{yyyyMMddHHmm}{5位随机}
     */
    public static String generate(String prefix, LocalDateTime createTime) {
        if (StringUtils.isBlank(prefix)) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        LocalDateTime minute = createTime != null ? createTime : LocalDateTime.now();
        int random = ThreadLocalRandom.current().nextInt(100_000);
        return prefix.trim().toUpperCase()
                + minute.format(MINUTE_FORMAT)
                + String.format("%05d", random);
    }

    /**
     * 防篡改校验：解析编码中的时间串，与 createTime 截断到分钟后比对
     */
    public static void assertMatchesCreateTime(String code, LocalDateTime createTime) {
        if (StringUtils.isBlank(code)) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        if (createTime == null) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_TAMPER_CODE, ErrorConfig.TEMPLATE_CODE_TAMPER_MSG);
        }
        String encodedMinute = extractMinutePart(code);
        String expectedMinute = createTime.withSecond(0).withNano(0).format(MINUTE_FORMAT);
        if (!expectedMinute.equals(encodedMinute)) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_TAMPER_CODE, ErrorConfig.TEMPLATE_CODE_TAMPER_MSG);
        }
    }

    /**
     * 从统一格式编号中提取时间部分（yyyyMMddHHmm）
     * 格式：{2位前缀}{12位时间}{5位随机} = 19 位
     */
    public static String extractMinutePart(String code) {
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        return code.substring(2, 14);
    }

    /**
     * 从统一格式编号中提取前缀部分
     */
    public static String extractPrefix(String code) {
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new EIException(ErrorConfig.TEMPLATE_CODE_INVALID_CODE, ErrorConfig.TEMPLATE_CODE_INVALID_MSG);
        }
        return code.substring(0, 2);
    }
}