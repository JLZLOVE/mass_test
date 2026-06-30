package untiy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import untiy.filter.JwtFilter;
import untiy.service.impl.UserDetailServiceImpl;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*    @Override
        protected void configure(HttpSecurity http) throws Exception {
            // 关闭所有安全拦截：允许所有请求通过，不做任何认证
            http.authorizeRequests().anyRequest().permitAll()
                    .and().csrf().disable(); // 关闭CSRF防护（DELETE请求需要）
        }*/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 配置使用你的 UserDetailService 和 PasswordEncoder
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }

    // ✅ 关键：让 Swagger/静态资源 完全不走 Security 过滤器链（彻底避免被拦截）
    @Override
    public void configure(WebSecurity web) throws Exception {
 /*       web.ignoring()
                .antMatchers(
                        "/v2/api-docs",
                        "/v2/api-docs/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/doc.html",
                        "/webjars/**",
                        "/favicon.ico",
                        "/js/**",
                        "/css/**",
                        "/img/**"
                )
                .antMatchers("/js/**", "/css/**", "/img/**");*/
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // ✅ 兜底放行：确保即使走了过滤器链，也能放行 Swagger
                .antMatchers(
                        "/v2/api-docs/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/js/**",
                        "/css/**",
                        "/img/**"
                        ,  "/doc.html"
                        ).permitAll()
                // ✅ 放行业务接口（登录/分配等）
                .antMatchers("/allocation/**", "/login/**", "/register/**", "/webjars/**").permitAll()
                // 其他接口必须认证
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}