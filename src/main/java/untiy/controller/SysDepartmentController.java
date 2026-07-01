package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysDepartment;
import untiy.service.SysDepartmentService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 社团部门表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@Tag(name = "社团部门管理", description = "社团部门相关接口，包含查询、新增、更新、删除等操作（前端仅开放查询）")
@RequestMapping("/sys-department")
public class SysDepartmentController {

    @Autowired
    private SysDepartmentService sysDepartmentService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有社团部门列表", description = "返回全部社团部门记录，无分页参数")
    @GetMapping("/listSysDepartment")
    public R listSysDepartment() {
        QueryWrapper<SysDepartment> ew = new QueryWrapper<>();
        List<SysDepartment> list = sysDepartmentService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询社团部门", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listSysDepartment_F")
    public R listSysDepartment_F(@RequestParam Map<String, Object> param, SysDepartment sysDepartment) {
        QueryWrapper<SysDepartment> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysDepartment);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysDepartment> page = MPUtil.getPage(param);
        IPage<SysDepartment> page1 = sysDepartmentService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询社团部门", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listSysDepartment_B")
    public R listSysDepartment_B(@RequestParam Map<String, Object> param, SysDepartment sysDepartment) {
        Page<SysDepartment> page = MPUtil.getPage(param);
        IPage<SysDepartment> page1 = sysDepartmentService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysDepartment),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询社团部门", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysDepartment sysDepartment) {
        QueryWrapper<SysDepartment> queryWrapper = new QueryWrapper<>();
        List<SysDepartment> list = sysDepartmentService.list(MPUtil.likeOrEq(queryWrapper, sysDepartment));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询社团部门（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysDepartment_B/{id}")
    public R detailSysDepartment_B(@PathVariable("id") Long id) {
        SysDepartment obj = sysDepartmentService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询社团部门（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysDepartment_F/{id}")
    public R detailSysDepartment_F(@PathVariable("id") Long id) {
        SysDepartment obj = sysDepartmentService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增社团部门（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysDepartment sysDepartment) {
        sysDepartmentService.save(sysDepartment);
        return R.ok("添加成功").put("data", sysDepartment);
    }

    /**
     * 前端增加（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysDepartment sysDepartment) {
            sysDepartmentService.save(sysDepartment);
        return R.ok("添加成功").put("data", sysDepartment);
    }
    */

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新社团部门（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysDepartment_B")
    public R updateSysDepartment_B(@Valid @RequestBody List<SysDepartment> sysDepartments) {
        sysDepartmentService.updateBatchById(sysDepartments);
        return R.ok();
    }

    /**
     * 前端单个更新（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @IgnoreAuth
    @PutMapping("/updateSysDepartment_F")
    public R updateSysDepartment_F(@Valid @RequestBody SysDepartment sysDepartment) {
            sysDepartmentService.updateById(sysDepartment);
        return R.ok();
    }
    */

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除社团部门（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysDepartment_B")
    public R deleteSysDepartment_B(@RequestBody List<Long> ids) {
        sysDepartmentService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开） - 已注释，前端仅保留查询功能
     */
    /*
    @DeleteMapping("/deleteSysDepartment_F/{id}")
    public R deleteSysDepartment_F(@PathVariable Long id) {
            sysDepartmentService.removeById(id);
        return R.ok();
    }
    */
}