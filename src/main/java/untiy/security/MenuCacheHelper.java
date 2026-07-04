package untiy.security;

import org.springframework.data.redis.core.RedisTemplate;
import untiy.entity.vo.MenuTreeResultVO;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 菜单树 Redis 缓存。按 userId 缓存（非 effectiveLevel），因同等级用户角色绑定可能不同。
 */
public final class MenuCacheHelper {

    public static final String KEY_PREFIX = "menu:tree:";
    private static final long CACHE_TTL_HOURS = 1;

    private MenuCacheHelper() {
    }

    public static String keyForUser(Long userId) {
        return KEY_PREFIX + userId;
    }

    public static MenuTreeResultVO get(RedisTemplate<String, Object> redisTemplate, Long userId) {
        if (redisTemplate == null || userId == null) {
            return null;
        }
        Object cached = redisTemplate.opsForValue().get(keyForUser(userId));
        if (cached instanceof MenuTreeResultVO) {
            return (MenuTreeResultVO) cached;
        }
        return null;
    }

    public static void put(RedisTemplate<String, Object> redisTemplate, Long userId, MenuTreeResultVO result) {
        if (redisTemplate == null || userId == null || result == null) {
            return;
        }
        redisTemplate.opsForValue().set(keyForUser(userId), result, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    /** 菜单增删改后清除全部用户菜单树缓存 */
    public static void evictAll(RedisTemplate<String, Object> redisTemplate) {
        if (redisTemplate == null) {
            return;
        }
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
