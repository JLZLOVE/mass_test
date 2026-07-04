package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.SysRole;
import untiy.exception.Level;
import untiy.service.SysRoleService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 角色管理：每个业务功能仅保留单一接口，权限差异由 Service 层处理。
 * 严格对标 SysUserController 设计。
 */
@RestController
@Tag(name = "角色管理", description = "角色 CRUD，列表自动按当前用户等级过滤")
@RequestMapping("/sys-role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    // ==============查询角色==============================
    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "分页查询角色", description = "统一查询入口，自动按权限等级过滤（role_level >= 当前用户等级）")
    @GetMapping("/listSysRole")
    public R listSysRole(@RequestParam Map<String, Object> param, SysRole sysRole) {
        IPage<SysRole> page = sysRoleService.pageQuery(param, sysRole);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "查询角色详情", description = "受等级限制，只能查看低于或等于自身等级的角色")
    @GetMapping("/detailSysRole/{id}")
    public R detailSysRole(@PathVariable Long id) {
        SysRole role = sysRoleService.getDetail(id);
        return R.ok().put("data", role);
    }

    // ==============新增角色==============================
    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "新增角色", description = "管理员及以上可调用，不能创建高于自身等级的角色")
    @PostMapping("/addSysRole")
    public R addSysRole(@Valid @RequestBody SysRole sysRole) {
        sysRoleService.saveRole(sysRole);
        // 返回新增后的详情（可选）
        SysRole created = sysRoleService.getDetail(sysRole.getId());
        return R.ok("添加成功").put("data", created);
    }

    // ==============更新角色==============================
    // 批量更新
    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "批量更新角色", description = "管理员及以上可调用，仅能修改低于或等于自身等级的角色，不能提权")
    @PutMapping("/updateSysRoleBatc")
    public R updateSysRoleBatch(@Valid @RequestBody List<SysRole> sysRoles) {
        sysRoleService.updateRoles(sysRoles);
        return R.ok();
    }

    // 单个更新（同样需要 ADMIN，因为角色不属于个人）
    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "更新角色", description = "管理员及以上可调用，权限校验同批量更新")
    @PutMapping("/updateSysRole")
    public R updateSysRole(@Valid @RequestBody SysRole sysRole) {
        sysRoleService.updateRole(sysRole);
        return R.ok();
    }

    // ==============删除角色==============================
    // 单个删除
    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "根据ID删除角色", description = "管理员及以上可调用，不能删除高于自身等级的角色，也不能删除自己拥有的角色")
    @DeleteMapping("/deleteSysRole/{id}")
    public R deleteSysRoleById(@PathVariable Long id) {
        sysRoleService.deleteById(id);
        return R.ok("删除成功");
    }

    // 批量删除
    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "根据ID批量删除角色", description = "管理员及以上可调用，逐条校验权限")
    @DeleteMapping("/deleteSysRole")
    public R deleteSysRoleBatch(@RequestBody List<Long> ids) {
        sysRoleService.deleteByIds(ids);
        return R.ok();
    }
}