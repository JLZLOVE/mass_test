package untiy.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String permissionCode;
    private String componentPath;
    private String routePath;
    private String icon;
    private Integer sort;
    private Integer status;

    private List<MenuTreeVO> children = new ArrayList<>();
}
