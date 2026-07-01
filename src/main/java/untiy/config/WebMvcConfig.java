package untiy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 配置：注册 {@link IgnoreAuthInterceptor}。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private IgnoreAuthInterceptor ignoreAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ignoreAuthInterceptor).addPathPatterns("/**");
    }
}
