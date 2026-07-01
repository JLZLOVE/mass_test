package untiy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import untiy.annotation.IgnoreAuth;
import untiy.annotation.IgnoreAuthConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC 拦截器：Handler 解析完成后识别 {@link IgnoreAuth}，写入请求标记。
 * <p>
 * 供业务层或后续组件读取；JwtFilter 主要依赖 {@link IgnoreAuthRegistry} 启动扫描匹配，
 * 此处作为 DispatcherServlet 阶段的二次确认与标记补充。
 */
@Component
public class IgnoreAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IgnoreAuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (hasIgnoreAuth(handlerMethod)) {
            request.setAttribute(IgnoreAuthConstants.REQUEST_ATTR, Boolean.TRUE);
            log.debug("Interceptor 标记 @IgnoreAuth: {} {}",
                    request.getMethod(), request.getServletPath());
        }
        return true;
    }

    private boolean hasIgnoreAuth(HandlerMethod handlerMethod) {
        if (handlerMethod.getMethodAnnotation(IgnoreAuth.class) != null) {
            return true;
        }
        return handlerMethod.getBeanType().getAnnotation(IgnoreAuth.class) != null;
    }
}
