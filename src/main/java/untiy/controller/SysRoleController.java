package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysRole;
import untiy.service.SysRoleService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@Tag(name = "角色管理", description = "角色相关接口，包含查询、新增、更新、删除等操作（前端仅开放查询）")
@RequestMapping("/sys-role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有角色列表", description = "返回全部角色记录，无分页参数")
    @GetMapping("/listSysRole")
    public R listSysRole() {
        QueryWrapper<SysRole> ew = new QueryWrapper<>();
        List<SysRole> list = sysRoleService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询角色", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listSysRole_F")
    public R listSysRole_F(@RequestParam Map<String, Object> param, SysRole sysRole) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysRole);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysRole> page = MPUtil.getPage(param);
        IPage<SysRole> page1 = sysRoleService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询角色", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listSysRole_B")
    public R listSysRole_B(@RequestParam Map<String, Object> param, SysRole sysRole) {
        Page<SysRole> page = MPUtil.getPage(param);
        IPage<SysRole> page1 = sysRoleService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysRole),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询角色", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysRole sysRole) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        List<SysRole> list = sysRoleService.list(MPUtil.likeOrEq(queryWrapper, sysRole));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询角色（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysRole_B/{id}")
    public R detailSysRole_B(@PathVariable("id") Long id) {
        SysRole obj = sysRoleService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询角色（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysRole_F/{id}")
    public R detailSysRole_F(@PathVariable("id") Long id) {
        SysRole obj = sysRoleService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增角色（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return R.ok("添加成功").put("data", sysRole);
    }

    /**
     * 前端增加（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysRole sysRole) {
            sysRoleService.save(sysRole);
        return R.ok("添加成功").put("data", sysRole);
    }
    */

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新角色（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysRole_B")
    public R updateSysRole_B(@Valid @RequestBody List<SysRole> sysRoles) {
        sysRoleService.updateBatchById(sysRoles);
        return R.ok();
    }

    /**
     * 前端单个更新（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PutMapping("/updateSysRole_F")
    public R updateSysRole_F(@Valid @RequestBody SysRole sysRole) {
            sysRoleService.updateById(sysRole);
        return R.ok();
    }
    */

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除角色（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysRole_B")
    public R deleteSysRole_B(@RequestBody List<Long> ids) {
        sysRoleService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @DeleteMapping("/deleteSysRole_F/{id}")
    public R deleteSysRole_F(@PathVariable Long id) {
            sysRoleService.removeById(id);
        return R.ok();
    }
    */
}