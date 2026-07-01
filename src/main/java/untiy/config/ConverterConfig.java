package untiy.config;  // 或者放到合适的位置

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import untiy.converter.SysUserConverter;
import untiy.converter.SysUserConverterImpl;  // 重要：导入生成的实现类

@Configuration
public class ConverterConfig {
    @Bean
    public SysUserConverter sysUserConverter() {
        return new SysUserConverterImpl();
    }
}
