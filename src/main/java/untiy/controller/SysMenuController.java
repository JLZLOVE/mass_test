package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/sys-menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(SysMenu sysMenu) {
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        List<SysMenu> list = sysMenuService.list(MPUtil.likeOrEq(queryWrapper, sysMenu));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysMenu_B/{id}")
    public R detailSysMenu_B(@PathVariable("id") Long id) {
        SysMenu obj = sysMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysMenu_F/{id}")
    public R detailSysMenu_F(@PathVariable("id") Long id) {
        SysMenu obj = sysMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysMenu sysMenu) {
            sysMenuService.save(sysMenu);
        return R.ok("添加成功").put("data", sysMenu);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysMenu sysMenu) {
            sysMenuService.save(sysMenu);
        return R.ok("添加成功").put("data", sysMenu);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysMenu_B")
    public R updateSysMenu_B(@Valid @RequestBody List<SysMenu> sysMenus) {
            sysMenuService.updateBatchById(sysMenus);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysMenu_F")
    public R updateSysMenu_F(@Valid @RequestBody SysMenu sysMenu) {
            sysMenuService.updateById(sysMenu);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysMenu_B")
    public R deleteSysMenu_B(@RequestBody List<Long> ids) {
            sysMenuService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysMenu_F/{id}")
    public R deleteSysMenu_F(@PathVariable Long id) {
            sysMenuService.removeById(id);
        return R.ok();
    }
}