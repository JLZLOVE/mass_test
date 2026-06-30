package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户管理", description = "用户相关接口，包含查询、新增、更新、删除等操作（前端仅开放查询）")
@RequestMapping("/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有用户列表", description = "返回全部用户记录，无分页参数")
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
    @Operation(summary = "前端公开查询用户", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询用户", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询用户", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysUser sysUser) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        List<SysUser> list = sysUserService.list(MPUtil.likeOrEq(queryWrapper, sysUser));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询用户（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysUser_B/{id}")
    public R detailSysUser_B(@PathVariable("id") Long id) {
        SysUser obj = sysUserService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询用户（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysUser_F/{id}")
    public R detailSysUser_F(@PathVariable("id") Long id) {
        SysUser obj = sysUserService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增用户（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysUser sysUser) {
        sysUserService.save(sysUser);
        return R.ok("添加成功").put("data", sysUser);
    }

    /**
     * 前端增加（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysUser sysUser) {
            sysUserService.save(sysUser);
        return R.ok("添加成功").put("data", sysUser);
    }
    */

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新用户（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysUser_B")
    public R updateSysUser_B(@Valid @RequestBody List<SysUser> sysUsers) {
        sysUserService.updateBatchById(sysUsers);
        return R.ok();
    }

    /**
     * 前端单个更新（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PutMapping("/updateSysUser_F")
    public R updateSysUser_F(@Valid @RequestBody SysUser sysUser) {
            sysUserService.updateById(sysUser);
        return R.ok();
    }
    */

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除用户（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysUser_B")
    public R deleteSysUser_B(@RequestBody List<Long> ids) {
        sysUserService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @DeleteMapping("/deleteSysUser_F/{id}")
    public R deleteSysUser_F(@PathVariable Long id) {
            sysUserService.removeById(id);
        return R.ok();
    }
    */
}