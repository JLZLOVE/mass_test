package untiy.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 社团类别枚举项（code + 中文标签）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubCategoryItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 类别前缀码，如 SZ / XS / CX */
    private String code;

    /** 中文标签，如 学术科技类 */
    private String label;
}
