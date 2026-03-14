package untiy.utils;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

public class CodeUtil {

    public static void main(String[] args) {
        // 1. 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 2. 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("玖");
        gc.setOpen(false);
        gc.setFileOverride(true);
        mpg.setGlobalConfig(gc);

        // 3. 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://127.0.0.1:3306/mass_test1?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        mpg.setDataSource(dsc);

        // 4. 包配置（重要：这里的 parent 是 untiy）
        PackageConfig pc = new PackageConfig();
        pc.setParent("untiy");
        pc.setEntity("entity");
        pc.setService("service");
        pc.setController("controller");
        mpg.setPackageInfo(pc);

        // 5. 模板配置：明确指定使用自定义 Controller 模板
        TemplateConfig templateConfig = new TemplateConfig()
                .setEntity(null)
                .setMapper(null)
                .setService(null)
                .setServiceImpl(null)
                .setXml(null)
                .setController("/templates/controller.java.vm"); // 👈 必须加
        mpg.setTemplate(templateConfig);

        // 6. 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setRestControllerStyle(true);
        strategy.setInclude(
                "sys_user", "sys_college", "sys_major", "sys_club", "sys_department",
                "sys_role", "sys_menu", "sys_user_role", "sys_role_menu", "sys_data_permission",
                "notice_category", "notice_info", "notice_read_record",
                "activity_category", "activity_apply", "activity_approve_flow", "activity_sign",
                "club_statistics"
        );
        mpg.setStrategy(strategy);

        // 7. 执行生成
        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();

        System.out.println("生成完成！请检查目录：" + gc.getOutputDir() + "/untiy/controller");
    }
}