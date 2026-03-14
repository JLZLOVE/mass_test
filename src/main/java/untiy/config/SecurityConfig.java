package untiy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 关闭所有安全拦截：允许所有请求通过，不做任何认证
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable(); // 关闭CSRF防护（DELETE请求需要）
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 12 是加密强度（默认是10），数值越大计算越慢也越安全 [citation:4][citation:6]
        return new BCryptPasswordEncoder(12);
    }
}
