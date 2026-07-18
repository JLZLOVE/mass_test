package untiy.entity.constants;

import untiy.entity.ClubCategory;

/**
 * 模板编号前缀：统一使用社团6类前缀。
 * <p>
 * 格式：{前缀}{yyyyMMddHHmm}{5位随机}（19位），例 WH20260718143082739
 */
public final class TemplateCodePrefix {

    /** 默认模板前缀（通告类无归属社团时使用） */
    public static final String DEFAULT = "TO";

    /** 6 类社团前缀（与 ClubCategory 一致，2位缩写） */
    public static final String IDEOLOGY = "SZ";
    public static final String ACADEMIC = "XS";
    public static final String INNOVATION = "CX";
    public static final String CULTURE_SPORTS = "WH";
    public static final String VOLUNTEER = "GY";
    public static final String SELF_DISCIPLINE = "ZL";

    private TemplateCodePrefix() {
    }

    /**
     * 根据社团类别获取前缀，不在六类中则返回默认前缀
     */
    public static String fromCategory(String category) {
        if (ClubCategory.isValid(category)) {
            return ClubCategory.prefixOf(category);
        }
        return DEFAULT;
    }
}