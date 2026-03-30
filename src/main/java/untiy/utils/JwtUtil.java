package untiy.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import untiy.exception.ErrorConfig;
import untiy.config.JwtConfig;
import untiy.exception.EIException;

import java.util.Date;
@Component
@AllArgsConstructor
public class JwtUtil {
    private final JwtConfig jwtConfig;


    //    声称token
//    用户学号和工号
    public String generateToken(String userId) {
        String compact = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
        return compact;
    }

    //    解析token
    public String getUserIdFromToken(String token) {
        if (token == null) {
            throw new EIException(ErrorConfig.TOKEN_EXPIRED_CODE, ErrorConfig.TOKEN_EXPIRED_MSG);
        }

        String subject = Jwts.parser()
                .setSigningKey(jwtConfig.getSecret())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return subject;
    }

    //    验证token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new EIException(
                    ErrorConfig.TOKEN_FLASE_MSG,
                    ErrorConfig.TOKEN_FLASE_CODE,e);
        } catch (JwtException e) {
            throw new EIException(ErrorConfig.TOKEN_MISSING_MSG,
                    ErrorConfig.TOKEN_MISSING, e);
        }
    }
}



