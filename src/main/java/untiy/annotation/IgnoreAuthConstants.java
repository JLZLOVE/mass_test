package untiy.annotation;

/**
 * {@link IgnoreAuth} 请求标记常量。
 * <p>
 * Filter 在 DispatcherServlet 之前执行，无法运行时解析 Handler；
 * 启动时由 {@link untiy.config.IgnoreAuthRegistry} 扫描路径，Filter 匹配后写入此属性。
 */
public final class IgnoreAuthConstants {

    /** 请求属性：当前接口免 Token 鉴权 */
    public static final String REQUEST_ATTR = "IGNORE_AUTH";

    private IgnoreAuthConstants() {
    }
}
