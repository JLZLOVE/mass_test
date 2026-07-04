package untiy.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class MenuTreeResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<MenuTreeVO> tree = new ArrayList<>();
    private Set<String> permissions = new HashSet<>();
}
