package untiy.entity;

import lombok.Data;

@Data
public class BaseQuery {
    private Integer page;
    private Integer limit;
    private String sidx;
    private String order;

    // 数据范围过滤字段（AOP自动注入，业务层无感知）
    private Long userId;        // 等级4 → 设当前用户ID
    private Long clubId;        // 等级2 → 设当前用户所属社团ID
    private Long departmentId;  // 等级3 → 设当前用户所属部门ID
    private Integer userType;   // 等级1 → 设1（学生）

}