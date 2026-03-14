package untiy.annotion;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.time.LocalDateTime;   // 此处以 LocalDateTime 为例，可扩展
import java.util.Date;

public class StartBeforeEndValidator
        implements ConstraintValidator<StartBeforeEnd, Object> {  // 泛型改为 Object

    private String startField;
    private String endField;

    @Override
    public void initialize(StartBeforeEnd annotation) {
        this.startField = annotation.startField();
        this.endField = annotation.endField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) return true;

        try {
            // 获取字段对应的 getter 方法（约定字段名首字母大写）
            Object startValue = getProperty(object, startField);
            Object endValue = getProperty(object, endField);

            // 空值由 @NotNull 负责，此处跳过校验
            if (startValue == null || endValue == null) {
                return true;
            }

            // 统一转换为 Comparable 进行比较
            return compareDates(startValue, endValue) < 0;

        } catch (Exception e) {
            throw new RuntimeException("无法校验 @StartBeforeEnd 属性", e);
        }
    }

    // 通过反射调用 getter 方法
    private Object getProperty(Object obj, String fieldName) throws Exception {
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = obj.getClass().getMethod(methodName);
        return method.invoke(obj);
    }

    // 比较两个日期/时间对象，支持常见日期类型
    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareDates(Object d1, Object d2) {
        // 若都是 Comparable 则直接比较
        if (d1 instanceof Comparable && d2 instanceof Comparable) {
            return ((Comparable) d1).compareTo(d2);
        }
        // 特殊处理 java.util.Date（它本身也是 Comparable，但为了清晰单独列出）
        if (d1 instanceof Date && d2 instanceof Date) {
            return ((Date) d1).compareTo((Date) d2);
        }
        // 可继续添加其他类型的转换逻辑，如 LocalDateTime -> toEpochSecond 等
        throw new IllegalArgumentException("不支持的时间类型：" + d1.getClass() + ", " + d2.getClass());
    }
}