package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysUserRole;
import untiy.service.SysUserRoleService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户角色关联表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/sys-user-role")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listSysUserRole")
    public R listSysUserRole() {
        QueryWrapper<SysUserRole> ew = new QueryWrapper<>();
        List<SysUserRole> list = sysUserRoleService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listSysUserRole_F")
    public R listSysUserRole_F(@RequestParam Map<String, Object> param, SysUserRole sysUserRole) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysUserRole);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysUserRole> page = MPUtil.getPage(param);             
        IPage<SysUserRole> page1 = sysUserRoleService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listSysUserRole_B")
    public R listSysUserRole_B(@RequestParam Map<String, Object> param, SysUserRole sysUserRole) {
        Page<SysUserRole> page = MPUtil.getPage(param);
        IPage<SysUserRole> page1 = sysUserRoleService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysUserRole),
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
    public R query(SysUserRole sysUserRole) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        List<SysUserRole> list = sysUserRoleService.list(MPUtil.likeOrEq(queryWrapper, sysUserRole));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysUserRole_B/{id}")
    public R detailSysUserRole_B(@PathVariable("id") Long id) {
        SysUserRole obj = sysUserRoleService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysUserRole_F/{id}")
    public R detailSysUserRole_F(@PathVariable("id") Long id) {
        SysUserRole obj = sysUserRoleService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysUserRole sysUserRole) {
            sysUserRoleService.save(sysUserRole);
        return R.ok("添加成功").put("data", sysUserRole);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysUserRole sysUserRole) {
            sysUserRoleService.save(sysUserRole);
        return R.ok("添加成功").put("data", sysUserRole);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysUserRole_B")
    public R updateSysUserRole_B(@Valid @RequestBody List<SysUserRole> sysUserRoles) {
            sysUserRoleService.updateBatchById(sysUserRoles);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysUserRole_F")
    public R updateSysUserRole_F(@Valid @RequestBody SysUserRole sysUserRole) {
            sysUserRoleService.updateById(sysUserRole);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysUserRole_B")
    public R deleteSysUserRole_B(@RequestBody List<Long> ids) {
            sysUserRoleService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysUserRole_F/{id}")
    public R deleteSysUserRole_F(@PathVariable Long id) {
            sysUserRoleService.removeById(id);
        return R.ok();
    }
}