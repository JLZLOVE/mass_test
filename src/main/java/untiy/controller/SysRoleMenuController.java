package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.dto.AssignRoleMenuDTO;
import untiy.exception.Level;
import untiy.service.SysRoleMenuService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色-菜单关联：全量覆盖分配与按角色查询。
 */
@RestController
@Tag(name = "角色菜单关联", description = "角色菜单全量分配与查询")
@RequestMapping("/sys-role-menu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "全量分配角色菜单", description = "先删后增，事务；空 menuIds 表示清空绑定")
    @PostMapping("/assign")
    public R assign(@Valid @RequestBody AssignRoleMenuDTO dto) {
        sysRoleMenuService.assign(dto);
        return R.ok("分配成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "查询角色已绑定菜单", description = "返回 menu_id 列表")
    @GetMapping("/listByRole/{roleId}")
    public R listByRole(@PathVariable Long roleId) {
        List<Long> menuIds = sysRoleMenuService.listMenuIdsByRole(roleId);
        return R.ok().put("data", menuIds);
    }
}
