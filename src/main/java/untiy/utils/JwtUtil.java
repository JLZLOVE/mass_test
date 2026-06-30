package untiy.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import untiy.config.JwtConfig;

import java.util.Date;

/**
 * JWT 工具类：负责 Token 签发、解析与校验。
 * <p>
 * 规范：Token 的 subject 仅存储 username（学号/工号），禁止存储数据库主键 ID。
 */
@Component
@AllArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    /**
     * 签发 JWT，subject 为 username（学号/工号）。
     *
     * @param username 学号或工号，作为 Token 唯一用户标识
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
    }

    /**
     * 从 Token 中解析 subject，即 username（学号/工号）。
     *
     * @param token JWT 字符串
     * @return username
     * @throws JwtException Token 无效、过期或签名错误时抛出
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtConfig.getSecret())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * @deprecated 语义已变更，subject 存储的是 username 而非 userId，请使用 {@link #getUsernameFromToken(String)}
     */
    @Deprecated
    public String getUserIdFromToken(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * 校验 Token 签名与有效期。
     * <p>
     * 供过滤器使用：无效或过期时返回 false，不抛异常，便于统一返回 401。
     *
     * @param token JWT 字符串
     * @return true 表示 Token 有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否已过期（用于日志区分过期与其他无效原因）。
     *
     * @param token JWT 字符串
     * @return true 表示 Token 已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
