package untiy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import untiy.service.AuthorService;
@SpringBootTest
public class testAuthority {
@Autowired
    AuthorService authorService;
    @Test
    public void test_getAuthoritiesByUserId(){
        System.out.print( authorService.getAuthoritiesByUserId(1L));
    }


}
