package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysUser;
import untiy.service.SysUserService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户基础表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listSysUser")
    public R listSysUser() {
        QueryWrapper<SysUser> ew = new QueryWrapper<>();
        List<SysUser> list = sysUserService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listSysUser_F")
    public R listSysUser_F(@RequestParam Map<String, Object> param, SysUser sysUser) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysUser);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysUser> page = MPUtil.getPage(param);             
        IPage<SysUser> page1 = sysUserService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listSysUser_B")
    public R listSysUser_B(@RequestParam Map<String, Object> param, SysUser sysUser) {
        Page<SysUser> page = MPUtil.getPage(param);
        IPage<SysUser> page1 = sysUserService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysUser),
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
    public R query(SysUser sysUser) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        List<SysUser> list = sysUserService.list(MPUtil.likeOrEq(queryWrapper, sysUser));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysUser_B/{id}")
    public R detailSysUser_B(@PathVariable("id") Long id) {
        SysUser obj = sysUserService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysUser_F/{id}")
    public R detailSysUser_F(@PathVariable("id") Long id) {
        SysUser obj = sysUserService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysUser sysUser) {
            sysUserService.save(sysUser);
        return R.ok("添加成功").put("data", sysUser);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysUser sysUser) {
            sysUserService.save(sysUser);
        return R.ok("添加成功").put("data", sysUser);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysUser_B")
    public R updateSysUser_B(@Valid @RequestBody List<SysUser> sysUsers) {
            sysUserService.updateBatchById(sysUsers);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysUser_F")
    public R updateSysUser_F(@Valid @RequestBody SysUser sysUser) {
            sysUserService.updateById(sysUser);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysUser_B")
    public R deleteSysUser_B(@RequestBody List<Long> ids) {
            sysUserService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysUser_F/{id}")
    public R deleteSysUser_F(@PathVariable Long id) {
            sysUserService.removeById(id);
        return R.ok();
    }
}