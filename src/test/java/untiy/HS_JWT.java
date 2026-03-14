package untiy;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import untiy.entity.SysUser;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.KeyPair;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class HS_JWT {
    static SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    HashMap<String, Object> claim = new HashMap<>();

    SysUser sysUser;

    @Test
    public void test_born() {
        Calendar instance = Calendar.getInstance();
        Date now = instance.getTime();
        instance.add(Calendar.DATE, 4);
        Date delay = instance.getTime();
//    传入
        sysUser = new SysUser();
        sysUser.setId(134L);
        sysUser.setPassword("12345678");
        claim.put("id", sysUser.getId());
        claim.put("password", sysUser.getPassword());
        String key = Jwts.builder()
                .setClaims(claim)
                .setId(String.valueOf(claim.get("id")))
                .setSubject(String.valueOf(claim.get("password")))

                .setIssuedAt(now)
                .setExpiration(delay)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        System.out.println(key);

        JwtParser build = Jwts.parserBuilder().setSigningKey(secretKey).build();
        try {
            Jws<Claims> claimsJws = build.parseClaimsJws(key);
            String signature = claimsJws.getSignature();
            Claims body = claimsJws.getBody();
            JwsHeader header = claimsJws.getHeader();

            System.out.println("signature: "+ signature);
            System.out.println("body: "+ body);
            System.out.println("header: "+header);
        } catch (Exception e) {
            System.out.println("jwty验证失败");
        }
    }
}
