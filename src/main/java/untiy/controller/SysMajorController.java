package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysMajor;
import untiy.service.SysMajorService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 专业表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/sys-major")
public class SysMajorController {

    @Autowired
    private SysMajorService sysMajorService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listSysMajor")
    public R listSysMajor() {
        QueryWrapper<SysMajor> ew = new QueryWrapper<>();
        List<SysMajor> list = sysMajorService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listSysMajor_F")
    public R listSysMajor_F(@RequestParam Map<String, Object> param, SysMajor sysMajor) {
        QueryWrapper<SysMajor> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysMajor);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysMajor> page = MPUtil.getPage(param);             
        IPage<SysMajor> page1 = sysMajorService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listSysMajor_B")
    public R listSysMajor_B(@RequestParam Map<String, Object> param, SysMajor sysMajor) {
        Page<SysMajor> page = MPUtil.getPage(param);
        IPage<SysMajor> page1 = sysMajorService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysMajor),
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
    public R query(SysMajor sysMajor) {
        QueryWrapper<SysMajor> queryWrapper = new QueryWrapper<>();
        List<SysMajor> list = sysMajorService.list(MPUtil.likeOrEq(queryWrapper, sysMajor));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysMajor_B/{id}")
    public R detailSysMajor_B(@PathVariable("id") Long id) {
        SysMajor obj = sysMajorService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysMajor_F/{id}")
    public R detailSysMajor_F(@PathVariable("id") Long id) {
        SysMajor obj = sysMajorService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysMajor sysMajor) {
            sysMajorService.save(sysMajor);
        return R.ok("添加成功").put("data", sysMajor);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysMajor sysMajor) {
            sysMajorService.save(sysMajor);
        return R.ok("添加成功").put("data", sysMajor);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysMajor_B")
    public R updateSysMajor_B(@Valid @RequestBody List<SysMajor> sysMajors) {
            sysMajorService.updateBatchById(sysMajors);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysMajor_F")
    public R updateSysMajor_F(@Valid @RequestBody SysMajor sysMajor) {
            sysMajorService.updateById(sysMajor);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysMajor_B")
    public R deleteSysMajor_B(@RequestBody List<Long> ids) {
            sysMajorService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysMajor_F/{id}")
    public R deleteSysMajor_F(@PathVariable Long id) {
            sysMajorService.removeById(id);
        return R.ok();
    }
}