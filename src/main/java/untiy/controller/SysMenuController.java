package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.SysMenu;
import untiy.entity.vo.MenuTreeResultVO;
import untiy.exception.Level;
import untiy.service.SysMenuService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.Map;

/**
 * 菜单管理：tree / list / save / delete 四个接口。
 */
@RestController
@Tag(name = "菜单管理", description = "菜单树、分页管理、增删改")
@RequestMapping("/sys-menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "当前用户可见菜单树", description = "返回菜单树 + 按钮权限集合；按 userId 缓存")
    @GetMapping("/tree")
    public R tree() {
        MenuTreeResultVO result = sysMenuService.getMenuTreeForCurrentUser();
        return R.ok().put("data", result);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "分页查询菜单", description = "支持 menuName 模糊、menuType 过滤，平铺返回")
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> param, SysMenu sysMenu) {
        IPage<SysMenu> page = sysMenuService.pageQuery(param, sysMenu);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "新增或更新菜单", description = "校验父级、循环引用、同级名称唯一、类型字段；清除菜单缓存")
    @PostMapping("/save")
    public R save(@Valid @RequestBody SysMenu sysMenu) {
        sysMenuService.saveMenu(sysMenu);
        return R.ok("保存成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "删除菜单", description = "无子菜单且未被更高权限角色绑定时删除，并清理 sys_role_menu")
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return R.ok("删除成功");
    }
}
