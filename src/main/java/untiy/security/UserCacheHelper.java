package untiy.security;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;

/**
 * 用户 Redis 登录缓存。Key 规则与 {@link untiy.controller.LoginController} / {@link untiy.filter.JwtFilter} 一致。
 */
public final class UserCacheHelper {

    /** v2：配合 CacheSnapshot 全字段 @JsonProperty，与旧版 user:{username} 隔离 */
    public static final String REDIS_USER_KEY_PREFIX = "user:v2:";

    private UserCacheHelper() {
    }

    public static String keyForUsername(String username) {
        return REDIS_USER_KEY_PREFIX + username;
    }

    public static void evictByUsernames(RedisTemplate<String, Object> redisTemplate, Collection<String> usernames) {
        if (redisTemplate == null || usernames == null || usernames.isEmpty()) {
            return;
        }
        for (String username : usernames) {
            if (username != null && !username.isEmpty()) {
                redisTemplate.delete(keyForUsername(username));
                redisTemplate.delete("user:" + username);
            }
        }
    }
}
