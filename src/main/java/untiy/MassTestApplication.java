package untiy;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"untiy.mapper"})
public class MassTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MassTestApplication.class, args);
    /*    OrderItem orderItem = new OrderItem();
        // 设置排序字段为 id，升序排列
        orderItem.setColumn("id");
        orderItem.setAsc(true);

        System.out.println("排序字段：" + orderItem.getColumn());
        System.out.println("是否升序：" + orderItem.isAsc());
        System.out.println("OrderItem 类使用成功！");*/
    }

}
