package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysDepartment;
import untiy.service.SysDepartmentService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

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
@RequestMapping("/sys-department")
public class SysDepartmentController {

    @Autowired
    private SysDepartmentService sysDepartmentService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(SysDepartment sysDepartment) {
        QueryWrapper<SysDepartment> queryWrapper = new QueryWrapper<>();
        List<SysDepartment> list = sysDepartmentService.list(MPUtil.likeOrEq(queryWrapper, sysDepartment));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysDepartment_B/{id}")
    public R detailSysDepartment_B(@PathVariable("id") Long id) {
        SysDepartment obj = sysDepartmentService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysDepartment_F/{id}")
    public R detailSysDepartment_F(@PathVariable("id") Long id) {
        SysDepartment obj = sysDepartmentService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysDepartment sysDepartment) {
            sysDepartmentService.save(sysDepartment);
        return R.ok("添加成功").put("data", sysDepartment);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysDepartment sysDepartment) {
            sysDepartmentService.save(sysDepartment);
        return R.ok("添加成功").put("data", sysDepartment);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysDepartment_B")
    public R updateSysDepartment_B(@Valid @RequestBody List<SysDepartment> sysDepartments) {
            sysDepartmentService.updateBatchById(sysDepartments);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysDepartment_F")
    public R updateSysDepartment_F(@Valid @RequestBody SysDepartment sysDepartment) {
            sysDepartmentService.updateById(sysDepartment);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysDepartment_B")
    public R deleteSysDepartment_B(@RequestBody List<Long> ids) {
            sysDepartmentService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysDepartment_F/{id}")
    public R deleteSysDepartment_F(@PathVariable Long id) {
            sysDepartmentService.removeById(id);
        return R.ok();
    }
}