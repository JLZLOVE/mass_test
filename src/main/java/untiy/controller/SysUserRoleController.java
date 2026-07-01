package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysUserRole;
import untiy.service.SysUserRoleService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

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
@Tag(name = "用户角色关联管理", description = "用户角色关联相关接口，包含查询、新增、更新、删除等操作（前端仅开放查询）")
@RequestMapping("/sys-user-role")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有用户角色关联列表", description = "返回全部用户角色关联记录，无分页参数")
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
    @Operation(summary = "前端公开查询用户角色关联", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询用户角色关联", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询用户角色关联", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysUserRole sysUserRole) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        List<SysUserRole> list = sysUserRoleService.list(MPUtil.likeOrEq(queryWrapper, sysUserRole));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询用户角色关联（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysUserRole_B/{id}")
    public R detailSysUserRole_B(@PathVariable("id") Long id) {
        SysUserRole obj = sysUserRoleService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询用户角色关联（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysUserRole_F/{id}")
    public R detailSysUserRole_F(@PathVariable("id") Long id) {
        SysUserRole obj = sysUserRoleService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增用户角色关联（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysUserRole sysUserRole) {
        sysUserRoleService.save(sysUserRole);
        return R.ok("添加成功").put("data", sysUserRole);
    }

    /**
     * 前端增加（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysUserRole sysUserRole) {
            sysUserRoleService.save(sysUserRole);
        return R.ok("添加成功").put("data", sysUserRole);
    }
    */

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新用户角色关联（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysUserRole_B")
    public R updateSysUserRole_B(@Valid @RequestBody List<SysUserRole> sysUserRoles) {
        sysUserRoleService.updateBatchById(sysUserRoles);
        return R.ok();
    }

    /**
     * 前端单个更新（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PutMapping("/updateSysUserRole_F")
    public R updateSysUserRole_F(@Valid @RequestBody SysUserRole sysUserRole) {
            sysUserRoleService.updateById(sysUserRole);
        return R.ok();
    }
    */

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除用户角色关联（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysUserRole_B")
    public R deleteSysUserRole_B(@RequestBody List<Long> ids) {
        sysUserRoleService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @DeleteMapping("/deleteSysUserRole_F/{id}")
    public R deleteSysUserRole_F(@PathVariable Long id) {
            sysUserRoleService.removeById(id);
        return R.ok();
    }
    */
}