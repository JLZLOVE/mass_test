package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysCollege;
import untiy.service.SysCollegeService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 学院表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/sys-college")
public class SysCollegeController {

    @Autowired
    private SysCollegeService sysCollegeService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listSysCollege")
    public R listSysCollege() {
        QueryWrapper<SysCollege> ew = new QueryWrapper<>();
        List<SysCollege> list = sysCollegeService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listSysCollege_F")
    public R listSysCollege_F(@RequestParam Map<String, Object> param, SysCollege sysCollege) {
        QueryWrapper<SysCollege> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysCollege);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysCollege> page = MPUtil.getPage(param);             
        IPage<SysCollege> page1 = sysCollegeService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listSysCollege_B")
    public R listSysCollege_B(@RequestParam Map<String, Object> param, SysCollege sysCollege) {
        Page<SysCollege> page = MPUtil.getPage(param);
        IPage<SysCollege> page1 = sysCollegeService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysCollege),
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
    public R query(SysCollege sysCollege) {
        QueryWrapper<SysCollege> queryWrapper = new QueryWrapper<>();
        List<SysCollege> list = sysCollegeService.list(MPUtil.likeOrEq(queryWrapper, sysCollege));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysCollege_B/{id}")
    public R detailSysCollege_B(@PathVariable("id") Long id) {
        SysCollege obj = sysCollegeService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysCollege_F/{id}")
    public R detailSysCollege_F(@PathVariable("id") Long id) {
        SysCollege obj = sysCollegeService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysCollege sysCollege) {
            sysCollegeService.save(sysCollege);
        return R.ok("添加成功").put("data", sysCollege);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysCollege sysCollege) {
            sysCollegeService.save(sysCollege);
        return R.ok("添加成功").put("data", sysCollege);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysCollege_B")
    public R updateSysCollege_B(@Valid @RequestBody List<SysCollege> sysColleges) {
            sysCollegeService.updateBatchById(sysColleges);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysCollege_F")
    public R updateSysCollege_F(@Valid @RequestBody SysCollege sysCollege) {
            sysCollegeService.updateById(sysCollege);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysCollege_B")
    public R deleteSysCollege_B(@RequestBody List<Long> ids) {
            sysCollegeService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysCollege_F/{id}")
    public R deleteSysCollege_F(@PathVariable Long id) {
            sysCollegeService.removeById(id);
        return R.ok();
    }
}