package untiy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口访问准入校验：仅控制哪些权限等级的用户可以调用当前接口。
 * <p>
 * 数据行范围过滤、DTO 字段脱敏请在 Service 层实现，不在此注解中处理。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresLevel {

    /** 允许的最高数字等级（effectiveLevel 数值越小权限越高，须 ≤ minLevel 才可访问） */
    int minLevel() default 0;
}
