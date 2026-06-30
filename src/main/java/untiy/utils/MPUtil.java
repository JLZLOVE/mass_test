package untiy.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * MyBatis-Plus 工具类（适用于 3.2 版本）
 * 提供构建查询条件、分页、排序等常用方法
 */
public class MPUtil {

    /**
     * 将 Java 驼峰字段名转为数据库下划线列名（如 userId → user_id）。
     * QueryWrapper 使用字符串列名时会直接拼入 SQL，必须映射为真实列名。
     */
    public static String camelToUnderline(String camel) {
        if (StringUtils.isBlank(camel)) {
            return camel;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ==================== 分页相关 ====================

    /**
     * 根据请求参数构建分页对象
     * @param params 参数Map，应包含 page, limit, sidx, order
     * @param <T> 实体类型
     * @return Page<T> 分页对象
     */
    public static <T> Page<T> getPage(Map<String, Object> params) {
        long current = 1;
        long size = 10;
        if (params.get("page") != null) {
            current = Long.parseLong(params.get("page").toString());
        }
        if (params.get("limit") != null) {
            size = Long.parseLong(params.get("limit").toString());
        }

        Page<T> page = new Page<>(current, size);

        // 处理排序
        String sidx = (String) params.get("sidx");
        String order = (String) params.get("order");
        if (StringUtils.isNotBlank(sidx) && StringUtils.isNotBlank(order)) {
            String column = camelToUnderline(sidx);
            if ("asc".equalsIgnoreCase(order)) {
                page.addOrder(OrderItem.asc(column));
            } else {
                page.addOrder(OrderItem.desc(column));
            }
        }
        return page;
    }

    // ==================== 条件构造 ====================

    /**
     * 将实体中非空字段转换为查询条件：
     * - 字符串类型使用 LIKE（模糊查询）
     * - 其他类型使用 EQ（精确查询）
     * @param wrapper QueryWrapper
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 传入的 wrapper（方便链式调用）
     */
    public static <T> QueryWrapper<T> likeOrEq(QueryWrapper<T> wrapper, T entity) {
        if (entity == null) return wrapper;
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 排除 serialVersionUID 字段
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value;
            try {
                value = field.get(entity);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (value == null) continue;
            // 使用数据库列名（下划线），避免 BadSqlGrammarException
            String columnName = camelToUnderline(fieldName);
            if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                wrapper.like(columnName, value);
            } else if (!(value instanceof String)) {
                wrapper.eq(columnName, value);
            }
        }
        return wrapper;
    }

    /**
     * 处理范围查询（从 params 中提取 start 和 end 参数，对指定字段进行 between）
     * 约定：字段名为 fieldName，则范围参数名为 fieldName_start 和 fieldName_end
     * @param wrapper QueryWrapper
     * @param params 参数Map
     * @param fieldName 数据库字段名
     * @return wrapper
     */
    public static <T> QueryWrapper<T> between(QueryWrapper<T> wrapper, Map<String, Object> params, String fieldName) {
        Object start = params.get(fieldName + "_start");
        Object end = params.get(fieldName + "_end");
        if (start != null && end != null) {
            wrapper.between(fieldName, start, end);
        } else if (start != null) {
            wrapper.ge(fieldName, start);
        } else if (end != null) {
            wrapper.le(fieldName, end);
        }
        return wrapper;
    }

    /**
     * 重载：自动处理常见范围字段（如 createTime, updateTime），可根据需求扩展
     */
    public static <T> QueryWrapper<T> between(QueryWrapper<T> wrapper, Map<String, Object> params) {
        between(wrapper, params, camelToUnderline("createTime"));
        between(wrapper, params, camelToUnderline("updateTime"));
        return wrapper;
    }
/*
 拓展写法
 public static <T> QueryWrapper<T> between(QueryWrapper<T> wrapper, Map<String, Object> params) {
        if (params == null) return wrapper;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || StringUtils.isBlank(value.toString())) continue;

            if (key.endsWith("_start")) {
                String field = key.substring(0, key.length() - 6); // 去掉 "_start"
                wrapper.ge(field, value);
            } else if (key.endsWith("_end")) {
                String field = key.substring(0, key.length() - 4); // 去掉 "_end"
                wrapper.le(field, value);
            }
        }
        return wrapper;
    }*/
    /**
     * 处理排序（从 params 中提取 sidx 和 order，应用到 wrapper）
     * @param wrapper QueryWrapper
     * @param params 参数Map
     * @return wrapper
     */
    public static <T> QueryWrapper<T> sort(QueryWrapper<T> wrapper, Map<String, Object> params) {
        String sidx = (String) params.get("sidx");
        String order = (String) params.get("order");
        if (StringUtils.isNotBlank(sidx) && StringUtils.isNotBlank(order)) {
            String column = camelToUnderline(sidx);
            if ("asc".equalsIgnoreCase(order)) {
                wrapper.orderByAsc(column);
            } else {
                wrapper.orderByDesc(column);
            }
        }
        return wrapper;
    }

    // ==================== 精确匹配（allEq） ====================

    /**
     * 将实体中非空字段转换为 Map，键可加前缀（用于多表关联时区分字段）
     * @param entity 实体对象
     * @param prefix 前缀（例如 "user."），如果不需要前缀可传 null 或空字符串
     * @return Map<String, Object> 键为带前缀的字段名，值为字段值
     */
    public static Map<String, Object> allEQMapPre(Object entity, String prefix) {
        Map<String, Object> map = new HashMap<>();
        if (entity == null) return map;
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 排除 serialVersionUID
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value;
            try {
                value = field.get(entity);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (value != null) {
                String key = (prefix == null || prefix.isEmpty()) ? fieldName : prefix + "." + fieldName;
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 将实体中非空字段转换为 Map（无前缀）
     * @param entity 实体对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> allEQMap(Object entity) {
        return allEQMapPre(entity, null);
    }

    /**
     * 直接为 wrapper 添加 allEq 条件（无前缀）
     * @param wrapper QueryWrapper
     * @param entity 实体对象
     * @return wrapper
     */
    public static <T> QueryWrapper<T> allEq(QueryWrapper<T> wrapper, T entity) {
        Map<String, Object> map = allEQMap(entity);
        if (!map.isEmpty()) {
            wrapper.allEq(map);
        }
        return wrapper;
    }

    /**
     * 为 wrapper 添加 allEq 条件（带前缀）
     * @param wrapper QueryWrapper
     * @param entity 实体对象
     * @param prefix 前缀
     * @return wrapper
     */
    public static <T> QueryWrapper<T> allEqPre(QueryWrapper<T> wrapper, T entity, String prefix) {
        Map<String, Object> map = allEQMapPre(entity, prefix);
        if (!map.isEmpty()) {
            wrapper.allEq(map);
        }
        return wrapper;
    }
}