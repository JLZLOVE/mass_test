package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysRoleMenu;
import untiy.service.SysRoleMenuService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 角色菜单关联表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@Tag(name = "角色菜单关联管理", description = "角色菜单关联相关接口，包含查询、新增、更新、删除等操作（前端仅开放查询）")
@RequestMapping("/sys-role-menu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有角色菜单关联列表", description = "返回全部角色菜单关联记录，无分页参数")
    @GetMapping("/listSysRoleMenu")
    public R listSysRoleMenu() {
        QueryWrapper<SysRoleMenu> ew = new QueryWrapper<>();
        List<SysRoleMenu> list = sysRoleMenuService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询角色菜单关联", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listSysRoleMenu_F")
    public R listSysRoleMenu_F(@RequestParam Map<String, Object> param, SysRoleMenu sysRoleMenu) {
        QueryWrapper<SysRoleMenu> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysRoleMenu);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysRoleMenu> page = MPUtil.getPage(param);
        IPage<SysRoleMenu> page1 = sysRoleMenuService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询角色菜单关联", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listSysRoleMenu_B")
    public R listSysRoleMenu_B(@RequestParam Map<String, Object> param, SysRoleMenu sysRoleMenu) {
        Page<SysRoleMenu> page = MPUtil.getPage(param);
        IPage<SysRoleMenu> page1 = sysRoleMenuService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysRoleMenu),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询角色菜单关联", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysRoleMenu sysRoleMenu) {
        QueryWrapper<SysRoleMenu> queryWrapper = new QueryWrapper<>();
        List<SysRoleMenu> list = sysRoleMenuService.list(MPUtil.likeOrEq(queryWrapper, sysRoleMenu));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询角色菜单关联（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysRoleMenu_B/{id}")
    public R detailSysRoleMenu_B(@PathVariable("id") Long id) {
        SysRoleMenu obj = sysRoleMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询角色菜单关联（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysRoleMenu_F/{id}")
    public R detailSysRoleMenu_F(@PathVariable("id") Long id) {
        SysRoleMenu obj = sysRoleMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增角色菜单关联（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysRoleMenu sysRoleMenu) {
        sysRoleMenuService.save(sysRoleMenu);
        return R.ok("添加成功").put("data", sysRoleMenu);
    }

    /**
     * 前端增加（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysRoleMenu sysRoleMenu) {
            sysRoleMenuService.save(sysRoleMenu);
        return R.ok("添加成功").put("data", sysRoleMenu);
    }
    */

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新角色菜单关联（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysRoleMenu_B")
    public R updateSysRoleMenu_B(@Valid @RequestBody List<SysRoleMenu> sysRoleMenus) {
        sysRoleMenuService.updateBatchById(sysRoleMenus);
        return R.ok();
    }

    /**
     * 前端单个更新（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PutMapping("/updateSysRoleMenu_F")
    public R updateSysRoleMenu_F(@Valid @RequestBody SysRoleMenu sysRoleMenu) {
            sysRoleMenuService.updateById(sysRoleMenu);
        return R.ok();
    }
    */

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除角色菜单关联（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysRoleMenu_B")
    public R deleteSysRoleMenu_B(@RequestBody List<Long> ids) {
        sysRoleMenuService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @DeleteMapping("/deleteSysRoleMenu_F/{id}")
    public R deleteSysRoleMenu_F(@PathVariable Long id) {
            sysRoleMenuService.removeById(id);
        return R.ok();
    }
    */
}