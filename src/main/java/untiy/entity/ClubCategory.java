package untiy.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 社团性质枚举（固定六类，不可自定义）。
 */
public final class ClubCategory {

    /** 思想政治类 */
    public static final String IDEOLOGY = "思想政治类";
    /** 学术科技类 */
    public static final String ACADEMIC = "学术科技类";
    /** 创新创业类 */
    public static final String INNOVATION = "创新创业类";
    /** 文化体育类 */
    public static final String CULTURE_SPORTS = "文化体育类";
    /** 志愿公益类 */
    public static final String VOLUNTEER = "志愿公益类";
    /** 自律互助类 */
    public static final String SELF_DISCIPLINE = "自律互助类";

    private static final Map<String, String> PREFIX_BY_CATEGORY;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(IDEOLOGY, "SZ");
        map.put(ACADEMIC, "XS");
        map.put(INNOVATION, "CX");
        map.put(CULTURE_SPORTS, "WH");
        map.put(VOLUNTEER, "GY");
        map.put(SELF_DISCIPLINE, "ZL");
        PREFIX_BY_CATEGORY = Collections.unmodifiableMap(map);
    }

    private ClubCategory() {
    }

    public static List<String> all() {
        return Arrays.asList(
                IDEOLOGY, ACADEMIC, INNOVATION, CULTURE_SPORTS, VOLUNTEER, SELF_DISCIPLINE);
    }

    public static boolean isValid(String category) {
        return category != null && PREFIX_BY_CATEGORY.containsKey(category);
    }

    public static String prefixOf(String category) {
        if (!isValid(category)) {
            throw new IllegalArgumentException("无效的社团类别: " + category);
        }
        return PREFIX_BY_CATEGORY.get(category);
    }

    public static String allowedCategoriesText() {
        return String.join("、", all());
    }
}
