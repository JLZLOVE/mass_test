package untiy.config;   // 务必和你项目的包路径一致
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;

/**
 * Knife4j/Swagger 授权配置类
 * 作用：让Knife4j显示Authorize按钮，支持JWT Token授权
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        System.out.println("OpenApiConfig loaded!");
        // 1. 定义JWT授权规则
        SecurityScheme jwtScheme = new SecurityScheme()
                .name("Authorization")  // 请求头名称（对应JWT的Authorization）
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")       // 授权类型为bearer
                .bearerFormat("JWT")    // 格式为JWT
                .in(SecurityScheme.In.HEADER);  // 放在请求头中

        // 2. 配置文档基本信息
        Info info = new Info()
                .title("Mass_Test1 API文档")  // 文档标题
                .version("v1.0")             // 版本号
                .description("Mass_Test1项目接口文档（支持JWT授权）");  // 描述

        // 3. 组装OpenAPI配置
        return new OpenAPI()
                .info(info)
                // 添加授权规则到全局（所有接口默认需要授权）
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                // 绑定JWT授权方案
                // ✅ 正确写法：直接用导入的Components类
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", jwtScheme));
    }
    @PostConstruct
    public void init() {
        System.out.println("OpenApiConfig loaded!");
    }
}
