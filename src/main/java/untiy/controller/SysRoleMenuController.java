package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/sys-role-menu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(SysRoleMenu sysRoleMenu) {
        QueryWrapper<SysRoleMenu> queryWrapper = new QueryWrapper<>();
        List<SysRoleMenu> list = sysRoleMenuService.list(MPUtil.likeOrEq(queryWrapper, sysRoleMenu));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysRoleMenu_B/{id}")
    public R detailSysRoleMenu_B(@PathVariable("id") Long id) {
        SysRoleMenu obj = sysRoleMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysRoleMenu_F/{id}")
    public R detailSysRoleMenu_F(@PathVariable("id") Long id) {
        SysRoleMenu obj = sysRoleMenuService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysRoleMenu sysRoleMenu) {
            sysRoleMenuService.save(sysRoleMenu);
        return R.ok("添加成功").put("data", sysRoleMenu);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysRoleMenu sysRoleMenu) {
            sysRoleMenuService.save(sysRoleMenu);
        return R.ok("添加成功").put("data", sysRoleMenu);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysRoleMenu_B")
    public R updateSysRoleMenu_B(@Valid @RequestBody List<SysRoleMenu> sysRoleMenus) {
            sysRoleMenuService.updateBatchById(sysRoleMenus);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysRoleMenu_F")
    public R updateSysRoleMenu_F(@Valid @RequestBody SysRoleMenu sysRoleMenu) {
            sysRoleMenuService.updateById(sysRoleMenu);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysRoleMenu_B")
    public R deleteSysRoleMenu_B(@RequestBody List<Long> ids) {
            sysRoleMenuService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysRoleMenu_F/{id}")
    public R deleteSysRoleMenu_F(@PathVariable Long id) {
            sysRoleMenuService.removeById(id);
        return R.ok();
    }
}