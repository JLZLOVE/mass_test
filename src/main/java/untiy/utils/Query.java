package untiy.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashMap;
import java.util.Map;

/**
 * 分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Query extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer page = 1;
    /**
     * 每页条数
     */
    private Integer limit = 10;
    /**
     * 排序字段
     */
    private String sidx;
    /**
     * 排序方式（asc/desc）
     */
    private String order;

    /**
     * 将请求参数转为Map，并初始化分页参数
     */
    public Query(Map<String, Object> params) {
        this.putAll(params);

        // 分页参数
        if (params.get("page") != null) {
            this.page = Integer.parseInt(params.get("page").toString());
        }
        if (params.get("limit") != null) {
            this.limit = Integer.parseInt(params.get("limit").toString());
        }
        if (params.get("sidx") != null) {
            this.sidx = params.get("sidx").toString();
        }
        if (params.get("order") != null) {
            this.order = params.get("order").toString();
        }

        this.put("page", page);
        this.put("limit", limit);
        this.put("sidx", sidx);
        this.put("order", order);
    }
}