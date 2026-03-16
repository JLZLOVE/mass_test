package untiy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import untiy.config.JwtConfig;
import untiy.utils.JwtUtil;

@SpringBootTest
public class Jwttest {
    @Autowired
    JwtConfig jwtConfig;
    @Test
    public void testJwt() {
        JwtUtil util = new JwtUtil(jwtConfig);
        String s = util.generateToken("202320164602");
        System.out.println(s);


        boolean b = util.validateToken(s);
        if (b) {
            String userIdFromToken = util.getUserIdFromToken(s);
            System.out.println(userIdFromToken);
        }
    }
}
