package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "数据权限规则管理", description = "数据权限规则相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/sys-data-permission")
public class SysDataPermissionController {

    @Autowired
    private SysDataPermissionService sysDataPermissionService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有数据权限规则列表", description = "返回全部数据权限规则记录，无分页参数")
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
    @Operation(summary = "前端公开查询数据权限规则", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询数据权限规则", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询数据权限规则", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysDataPermission sysDataPermission) {
        QueryWrapper<SysDataPermission> queryWrapper = new QueryWrapper<>();
        List<SysDataPermission> list = sysDataPermissionService.list(MPUtil.likeOrEq(queryWrapper, sysDataPermission));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询数据权限规则（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysDataPermission_B/{id}")
    public R detailSysDataPermission_B(@PathVariable("id") Long id) {
        SysDataPermission obj = sysDataPermissionService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询数据权限规则（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysDataPermission_F/{id}")
    public R detailSysDataPermission_F(@PathVariable("id") Long id) {
        SysDataPermission obj = sysDataPermissionService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增数据权限规则（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysDataPermission sysDataPermission) {
        sysDataPermissionService.save(sysDataPermission);
        return R.ok("添加成功").put("data", sysDataPermission);
    }

    /**
     * 前端增加（公开）
     */
/*    @IgnoreAuth
    @Operation(summary = "新增数据权限规则（公开）", description = "用户自行提交，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysDataPermission sysDataPermission) {
        sysDataPermissionService.save(sysDataPermission);
        return R.ok("添加成功").put("data", sysDataPermission);
    }*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新数据权限规则（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysDataPermission_B")
    public R updateSysDataPermission_B(@Valid @RequestBody List<SysDataPermission> sysDataPermissions) {
        sysDataPermissionService.updateBatchById(sysDataPermissions);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
/*    @IgnoreAuth
    @Operation(summary = "更新单个数据权限规则（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateSysDataPermission_F")
    public R updateSysDataPermission_F(@Valid @RequestBody SysDataPermission sysDataPermission) {
        sysDataPermissionService.updateById(sysDataPermission);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除数据权限规则（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysDataPermission_B")
    public R deleteSysDataPermission_B(@RequestBody List<Long> ids) {
        sysDataPermissionService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
/*    @Operation(summary = "删除单个数据权限规则（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteSysDataPermission_F/{id}")
    public R deleteSysDataPermission_F(@PathVariable Long id) {
        sysDataPermissionService.removeById(id);
        return R.ok();
    }*/
}