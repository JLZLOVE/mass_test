package untiy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import untiy.annotation.IgnoreAuth;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 启动时扫描所有 {@link IgnoreAuth} 接口，注册 HTTP 方法 + 路径模式。
 * <p>
 * JwtFilter 在 DispatcherServlet 之前执行，不能调用 {@code handlerMapping.getHandler()}，
 * 因此采用「启动扫描 + Ant 路径匹配」代替运行时 Handler 解析。
 */
@Component
public class IgnoreAuthRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(IgnoreAuthRegistry.class);

    private final List<IgnoreAuthEntry> entries = new ArrayList<>();

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 避免父子容器重复扫描
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        RequestMappingHandlerMapping mapping;
        try {
            mapping = event.getApplicationContext().getBean(RequestMappingHandlerMapping.class);
        } catch (Exception e) {
            log.warn("未找到 RequestMappingHandlerMapping，@IgnoreAuth 路径注册跳过", e);
            return;
        }

        entries.clear();
        mapping.getHandlerMethods().forEach((info, handlerMethod) -> registerHandler(info, handlerMethod));
        log.info("@IgnoreAuth 路径注册完成，共 {} 条", entries.size());
    }

    private void registerHandler(RequestMappingInfo info, HandlerMethod handlerMethod) {
        if (!hasIgnoreAuth(handlerMethod)) {
            return;
        }

        Set<String> patterns = extractPatterns(info);
        if (patterns.isEmpty()) {
            log.warn("@IgnoreAuth 接口未解析到路径：{}.{}",
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName());
            return;
        }

        Set<RequestMethod> methods = extractMethods(info);
        for (RequestMethod method : methods) {
            for (String pattern : patterns) {
                entries.add(new IgnoreAuthEntry(method.name(), normalizePattern(pattern)));
                log.debug("注册 @IgnoreAuth: {} {}", method, pattern);
            }
        }
    }

    private boolean hasIgnoreAuth(HandlerMethod handlerMethod) {
        if (handlerMethod.getMethodAnnotation(IgnoreAuth.class) != null) {
            return true;
        }
        return handlerMethod.getBeanType().getAnnotation(IgnoreAuth.class) != null;
    }

    private Set<String> extractPatterns(RequestMappingInfo info) {
        Set<String> patterns = new LinkedHashSet<>();
        PatternsRequestCondition patternsCondition = info.getPatternsCondition();
        if (patternsCondition != null) {
            patterns.addAll(patternsCondition.getPatterns());
        }
        if (info.getPathPatternsCondition() != null) {
            info.getPathPatternsCondition().getPatterns()
                    .forEach(p -> patterns.add(p.getPatternString()));
        }
        return patterns;
    }

    private Set<RequestMethod> extractMethods(RequestMappingInfo info) {
        RequestMethodsRequestCondition methodsCondition = info.getMethodsCondition();
        if (methodsCondition == null || methodsCondition.getMethods().isEmpty()) {
            return EnumSet.allOf(RequestMethod.class);
        }
        return methodsCondition.getMethods();
    }

    /**
     * 判断当前请求是否命中已注册的 @IgnoreAuth 路径。
     */
    public boolean matches(HttpServletRequest request) {
        String lookupPath = resolveLookupPath(request);
        String httpMethod = request.getMethod();
        for (IgnoreAuthEntry entry : entries) {
            if (entry.matches(httpMethod, lookupPath, pathMatcher)) {
                log.debug("@IgnoreAuth 路径命中: {} {} (pattern={})", httpMethod, lookupPath, entry.pattern);
                return true;
            }
        }
        return false;
    }

    /**
     * 解析相对 Servlet 的路径（已剥离 context-path，如 /Mass_Test）。
     */
    public String resolveLookupPath(HttpServletRequest request) {
        urlPathHelper.setAlwaysUseFullPath(false);
        String lookupPath = urlPathHelper.getLookupPathForRequest(request);
        if (lookupPath != null && !lookupPath.isEmpty()) {
            return normalizePattern(lookupPath);
        }
        String path = request.getServletPath();
        if (request.getPathInfo() != null) {
            path += request.getPathInfo();
        }
        return normalizePattern(path);
    }

    private static String normalizePattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return "/";
        }
        return pattern.startsWith("/") ? pattern : "/" + pattern;
    }

    public List<IgnoreAuthEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    private static final class IgnoreAuthEntry {
        private final String method;
        private final String pattern;

        private IgnoreAuthEntry(String method, String pattern) {
            this.method = method;
            this.pattern = pattern;
        }

        private boolean matches(String httpMethod, String lookupPath, AntPathMatcher matcher) {
            if (!method.equalsIgnoreCase(httpMethod)) {
                return false;
            }
            return matcher.match(pattern, lookupPath);
        }
    }
}
