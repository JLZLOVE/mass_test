package untiy.annotion;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartBeforeEndValidator.class)
public @interface StartBeforeEnd {

    // 开始时间字段名，默认为 "startTime"
    String startField() default "startTime";

    // 结束时间字段名，默认为 "endTime"
    String endField() default "endTime";

    String message() default "开始时间必须早于结束时间";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}