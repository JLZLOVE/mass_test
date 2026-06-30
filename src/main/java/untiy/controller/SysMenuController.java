package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysMenu;
import untiy.service.SysMenuService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 菜单权限表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@Tag(name = "菜单权限管理", description = "菜单权限相关接口，包含查询、新增、更新、删除等操作（前端仅开放查询）")
@RequestMapping("/sys-menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有菜单权限列表", description = "返回全部菜单权限记录，无分页参数")
    @GetMapping("/listSysMenu")
    public R listSysMenu() {
        QueryWrapper<SysMenu> ew = new QueryWrapper<>();
        List<SysMenu> list = sysMenuService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询菜单权限", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listSysMenu_F")
    public R listSysMenu_F(@RequestParam Map<String, Object> param, SysMenu sysMenu) {
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysMenu);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysMenu> page = MPUtil.getPage(param);
        IPage<SysMenu> page1 = sysMenuService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询菜单权限", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listSysMenu_B")
    public R listSysMenu_B(@RequestParam Map<String, Object> param, SysMenu sysMenu) {
        Page<SysMenu> page = MPUtil.getPage(param);
        IPage<SysMenu> page1 = sysMenuService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysMenu),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询菜单权限", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysMenu sysMenu) {
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        List<SysMenu> list = sysMenuService.list(MPUtil.likeOrEq(queryWrapper, sysMenu));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询菜单权限（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysMenu_B/{id}")
    public R detailSysMenu_B(@PathVariable("id") Long id) {
        SysMenu obj = sysMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询菜单权限（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysMenu_F/{id}")
    public R detailSysMenu_F(@PathVariable("id") Long id) {
        SysMenu obj = sysMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增菜单权限（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysMenu sysMenu) {
        sysMenuService.save(sysMenu);
        return R.ok("添加成功").put("data", sysMenu);
    }

    /**
     * 前端增加（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysMenu sysMenu) {
            sysMenuService.save(sysMenu);
        return R.ok("添加成功").put("data", sysMenu);
    }
    */

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新菜单权限（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysMenu_B")
    public R updateSysMenu_B(@Valid @RequestBody List<SysMenu> sysMenus) {
        sysMenuService.updateBatchById(sysMenus);
        return R.ok();
    }

    /**
     * 前端单个更新（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PutMapping("/updateSysMenu_F")
    public R updateSysMenu_F(@Valid @RequestBody SysMenu sysMenu) {
            sysMenuService.updateById(sysMenu);
        return R.ok();
    }
    */

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除菜单权限（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysMenu_B")
    public R deleteSysMenu_B(@RequestBody List<Long> ids) {
        sysMenuService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @DeleteMapping("/deleteSysMenu_F/{id}")
    public R deleteSysMenu_F(@PathVariable Long id) {
            sysMenuService.removeById(id);
        return R.ok();
    }
    */
}