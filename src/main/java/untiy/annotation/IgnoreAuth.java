package untiy.annotation;

import java.lang.annotation.*;

/**
 * 忽略Token验证
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {

}
