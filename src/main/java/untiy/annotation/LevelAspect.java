package untiy.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import untiy.security.LoginUserDetails;

import java.lang.reflect.Method;

/**
 * 接口准入鉴权：仅校验 {@link RequiresLevel}，不介入数据过滤与字段脱敏。
 * <p>
 * 切入点使用 {@code @annotation(…) || @within(…)} 但不绑定参数（避免 Spring AOP 对 ||
 * 组合参数绑定支持不完善导致 NPE），改为在方法体内通过反射显式获取注解。
 * 优先取方法上的 {@link RequiresLevel}，若方法上没有则取类上的注解作为默认值。
 */
@Slf4j
@Aspect
@Component
public class LevelAspect {

    @Around("@annotation(untiy.annotation.RequiresLevel) || @within(untiy.annotation.RequiresLevel)")
    public Object checkAccess(ProceedingJoinPoint pjp) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUserDetails)) {
            throw new AccessDeniedException("未登录或会话无效");
        }

        LoginUserDetails user = (LoginUserDetails) auth.getPrincipal();

        // 显式反射获取注解：优先方法级，兜底类级
        RequiresLevel requiresLevel = resolveRequiresLevel(pjp);
        if (requiresLevel == null) {
            // 理论上不会走到这里（切入点已匹配），做防御性处理
            return pjp.proceed();
        }
        log.info("当前用户等级: {}, 接口要求等级: {}", user.getEffectiveLevel(), requiresLevel.minLevel());
        if (user.getEffectiveLevel() > requiresLevel.minLevel()) {
            throw new AccessDeniedException("权限不足，需要等级≤" + requiresLevel.minLevel());
        }

        return pjp.proceed();
    }

    /**
     * 优先从方法上获取 {@link RequiresLevel}，若方法上没有则从类上获取。
     */
    private RequiresLevel resolveRequiresLevel(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        // 优先取方法上的注解
        RequiresLevel annotation = method.getAnnotation(RequiresLevel.class);
        if (annotation != null) {
            return annotation;
        }

        // 兜底：取类上的注解
        Class<?> targetClass = pjp.getTarget().getClass();
        return targetClass.getAnnotation(RequiresLevel.class);
    }
}