package untiy;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class JWT_Study {

    static KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.ES256);
    static PrivateKey aPrivate = keyPair.getPrivate();
    static PublicKey aPublic = keyPair.getPublic();


    HashMap<String, Object> stringObjectHashMap = new HashMap<>();

    @Test
    public void getJWT_Token() {
        Calendar instance = Calendar.getInstance();
        Date now = instance.getTime();

        instance.add(Calendar.DATE, 4);

        Date expiration = instance.getTime();
        String sign = Jwts.builder()
                .setClaims(stringObjectHashMap)
                .setId("userId")
                .setSubject("password")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(aPrivate, SignatureAlgorithm.ES256)
                .compact();
        //               addPayloads(stringObjectHashMap);
        System.out.println(sign);
    }

    @Test
    public void test() {
        Calendar instance = Calendar.getInstance();
        Date now = instance.getTime();
        instance.add(Calendar.DATE, 5);
        Date expiration = instance.getTime();
        // 使用公钥构建解析器
        JwtParserBuilder parserBuilder = Jwts.parserBuilder().setSigningKey(aPublic);
        JwtParser parser = parserBuilder.build();
        String sign = Jwts.builder()
                .setClaims(stringObjectHashMap)
                .setId("userId")
                .setSubject("password")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(aPrivate, SignatureAlgorithm.ES256)
                .compact();
// 解析 JWT（假设 token 是由对应的私钥签发的）
        String token = sign;
        try {
            Jws<Claims> jws = parser.parseClaimsJws(token);
            Claims body = jws.getBody();
            String signature = jws.getSignature();
            JwsHeader header = jws.getHeader();

            System.out.println("Header: " + header);
            System.out.println("Body: " + body);
            System.out.println("Signature: " + signature);
        } catch (JwtException e) {
            System.out.println("JWT 验证失败: " + e.getMessage());
        }
    }

    @Test
    public void Undertstand() {

    }
}
