package untiy.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import untiy.security.LoginUserDetails;

/**
 * 接口准入鉴权：仅校验 {@link RequiresLevel}，不介入数据过滤与字段脱敏。
 */
@Aspect
@Component
public class LevelAspect {

    @Around("@annotation(requiresLevel) || @within(requiresLevel)")
    public Object checkAccess(ProceedingJoinPoint pjp, RequiresLevel requiresLevel) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUserDetails)) {
            throw new AccessDeniedException("未登录或会话无效");
        }

        LoginUserDetails user = (LoginUserDetails) auth.getPrincipal();
        if (user.getEffectiveLevel() > requiresLevel.minLevel()) {
            throw new AccessDeniedException("权限不足，需要等级≤" + requiresLevel.minLevel());
        }

        return pjp.proceed();
    }
}
