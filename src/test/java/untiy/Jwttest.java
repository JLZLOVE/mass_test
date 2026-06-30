package untiy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
            String usernameFromToken = util.getUsernameFromToken(s);
            System.out.println(usernameFromToken);
        }
    }
    @Test
    public void  testjwtpassword(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pwd = encoder.encode("123456");
        System.out.println(pwd);
    }
}
