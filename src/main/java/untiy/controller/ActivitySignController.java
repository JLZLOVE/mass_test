package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.ActivitySign;
import untiy.service.ActivitySignService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 活动签到表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/activity-sign")
public class ActivitySignController {

    @Autowired
    private ActivitySignService activitySignService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listActivitySign")
    public R listActivitySign() {
        QueryWrapper<ActivitySign> ew = new QueryWrapper<>();
        List<ActivitySign> list = activitySignService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listActivitySign_F")
    public R listActivitySign_F(@RequestParam Map<String, Object> param, ActivitySign activitySign) {
        QueryWrapper<ActivitySign> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, activitySign);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<ActivitySign> page = MPUtil.getPage(param);             
        IPage<ActivitySign> page1 = activitySignService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listActivitySign_B")
    public R listActivitySign_B(@RequestParam Map<String, Object> param, ActivitySign activitySign) {
        Page<ActivitySign> page = MPUtil.getPage(param);
        IPage<ActivitySign> page1 = activitySignService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), activitySign),
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
    public R query(ActivitySign activitySign) {
        QueryWrapper<ActivitySign> queryWrapper = new QueryWrapper<>();
        List<ActivitySign> list = activitySignService.list(MPUtil.likeOrEq(queryWrapper, activitySign));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailActivitySign_B/{id}")
    public R detailActivitySign_B(@PathVariable("id") Long id) {
        ActivitySign obj = activitySignService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailActivitySign_F/{id}")
    public R detailActivitySign_F(@PathVariable("id") Long id) {
        ActivitySign obj = activitySignService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivitySign activitySign) {
            activitySignService.save(activitySign);
        return R.ok("添加成功").put("data", activitySign);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ActivitySign activitySign) {
            activitySignService.save(activitySign);
        return R.ok("添加成功").put("data", activitySign);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateActivitySign_B")
    public R updateActivitySign_B(@Valid @RequestBody List<ActivitySign> activitySigns) {
            activitySignService.updateBatchById(activitySigns);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateActivitySign_F")
    public R updateActivitySign_F(@Valid @RequestBody ActivitySign activitySign) {
            activitySignService.updateById(activitySign);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteActivitySign_B")
    public R deleteActivitySign_B(@RequestBody List<Long> ids) {
            activitySignService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteActivitySign_F/{id}")
    public R deleteActivitySign_F(@PathVariable Long id) {
            activitySignService.removeById(id);
        return R.ok();
    }
}