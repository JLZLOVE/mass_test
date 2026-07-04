package untiy.security;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;

/**
 * 用户 Redis 登录缓存清理，Key 规则与 {@link untiy.controller.LoginController} / {@link untiy.filter.JwtFilter} 一致。
 */
public final class UserCacheHelper {

    public static final String REDIS_USER_KEY_PREFIX = "user:";

    private UserCacheHelper() {
    }

    public static void evictByUsernames(RedisTemplate<String, Object> redisTemplate, Collection<String> usernames) {
        if (redisTemplate == null || usernames == null || usernames.isEmpty()) {
            return;
        }
        for (String username : usernames) {
            if (username != null && !username.isEmpty()) {
                redisTemplate.delete(REDIS_USER_KEY_PREFIX + username);
            }
        }
    }
}
