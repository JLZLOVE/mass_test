package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysDataPermission;
import untiy.service.SysDataPermissionService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 数据权限规则表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/sys-data-permission")
public class SysDataPermissionController {

    @Autowired
    private SysDataPermissionService sysDataPermissionService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listSysDataPermission")
    public R listSysDataPermission() {
        QueryWrapper<SysDataPermission> ew = new QueryWrapper<>();
        List<SysDataPermission> list = sysDataPermissionService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listSysDataPermission_F")
    public R listSysDataPermission_F(@RequestParam Map<String, Object> param, SysDataPermission sysDataPermission) {
        QueryWrapper<SysDataPermission> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysDataPermission);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysDataPermission> page = MPUtil.getPage(param);             
        IPage<SysDataPermission> page1 = sysDataPermissionService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listSysDataPermission_B")
    public R listSysDataPermission_B(@RequestParam Map<String, Object> param, SysDataPermission sysDataPermission) {
        Page<SysDataPermission> page = MPUtil.getPage(param);
        IPage<SysDataPermission> page1 = sysDataPermissionService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysDataPermission),
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
    public R query(SysDataPermission sysDataPermission) {
        QueryWrapper<SysDataPermission> queryWrapper = new QueryWrapper<>();
        List<SysDataPermission> list = sysDataPermissionService.list(MPUtil.likeOrEq(queryWrapper, sysDataPermission));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysDataPermission_B/{id}")
    public R detailSysDataPermission_B(@PathVariable("id") Long id) {
        SysDataPermission obj = sysDataPermissionService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysDataPermission_F/{id}")
    public R detailSysDataPermission_F(@PathVariable("id") Long id) {
        SysDataPermission obj = sysDataPermissionService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysDataPermission sysDataPermission) {
            sysDataPermissionService.save(sysDataPermission);
        return R.ok("添加成功").put("data", sysDataPermission);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysDataPermission sysDataPermission) {
            sysDataPermissionService.save(sysDataPermission);
        return R.ok("添加成功").put("data", sysDataPermission);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysDataPermission_B")
    public R updateSysDataPermission_B(@Valid @RequestBody List<SysDataPermission> sysDataPermissions) {
            sysDataPermissionService.updateBatchById(sysDataPermissions);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysDataPermission_F")
    public R updateSysDataPermission_F(@Valid @RequestBody SysDataPermission sysDataPermission) {
            sysDataPermissionService.updateById(sysDataPermission);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysDataPermission_B")
    public R deleteSysDataPermission_B(@RequestBody List<Long> ids) {
            sysDataPermissionService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysDataPermission_F/{id}")
    public R deleteSysDataPermission_F(@PathVariable Long id) {
            sysDataPermissionService.removeById(id);
        return R.ok();
    }
}