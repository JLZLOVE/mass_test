package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import untiy.entity.SysMenu;
import untiy.entity.vo.MenuTreeResultVO;

import java.util.Map;

public interface SysMenuService extends IService<SysMenu> {

    MenuTreeResultVO getMenuTreeForCurrentUser();

    IPage<SysMenu> pageQuery(Map<String, Object> param, SysMenu query);

    void saveMenu(SysMenu menu);

    void deleteMenu(Long id);
}
