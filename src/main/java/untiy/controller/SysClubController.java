package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysClub;
import untiy.service.SysClubService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 社团表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/sys-club")
public class SysClubController {

    @Autowired
    private SysClubService sysClubService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listSysClub")
    public R listSysClub() {
        QueryWrapper<SysClub> ew = new QueryWrapper<>();
        List<SysClub> list = sysClubService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listSysClub_F")
    public R listSysClub_F(@RequestParam Map<String, Object> param, SysClub sysClub) {
        QueryWrapper<SysClub> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysClub);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysClub> page = MPUtil.getPage(param);             
        IPage<SysClub> page1 = sysClubService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listSysClub_B")
    public R listSysClub_B(@RequestParam Map<String, Object> param, SysClub sysClub) {
        Page<SysClub> page = MPUtil.getPage(param);
        IPage<SysClub> page1 = sysClubService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysClub),
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
    public R query(SysClub sysClub) {
        QueryWrapper<SysClub> queryWrapper = new QueryWrapper<>();
        List<SysClub> list = sysClubService.list(MPUtil.likeOrEq(queryWrapper, sysClub));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailSysClub_B/{id}")
    public R detailSysClub_B(@PathVariable("id") Long id) {
        SysClub obj = sysClubService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailSysClub_F/{id}")
    public R detailSysClub_F(@PathVariable("id") Long id) {
        SysClub obj = sysClubService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysClub sysClub) {
            sysClubService.save(sysClub);
        return R.ok("添加成功").put("data", sysClub);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysClub sysClub) {
            sysClubService.save(sysClub);
        return R.ok("添加成功").put("data", sysClub);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateSysClub_B")
    public R updateSysClub_B(@Valid @RequestBody List<SysClub> sysClubs) {
            sysClubService.updateBatchById(sysClubs);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateSysClub_F")
    public R updateSysClub_F(@Valid @RequestBody SysClub sysClub) {
            sysClubService.updateById(sysClub);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteSysClub_B")
    public R deleteSysClub_B(@RequestBody List<Long> ids) {
            sysClubService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteSysClub_F/{id}")
    public R deleteSysClub_F(@PathVariable Long id) {
            sysClubService.removeById(id);
        return R.ok();
    }
}