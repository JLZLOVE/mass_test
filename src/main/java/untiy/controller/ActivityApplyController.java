package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.ActivityApply;
import untiy.service.ActivityApplyService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 活动申请表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/activity-apply")
public class ActivityApplyController {

    @Autowired
    private ActivityApplyService activityApplyService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listActivityApply")
    public R listActivityApply() {
        QueryWrapper<ActivityApply> ew = new QueryWrapper<>();
        List<ActivityApply> list = activityApplyService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listActivityApply_F")
    public R listActivityApply_F(@RequestParam Map<String, Object> param, ActivityApply activityApply) {
        QueryWrapper<ActivityApply> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, activityApply);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<ActivityApply> page = MPUtil.getPage(param);             
        IPage<ActivityApply> page1 = activityApplyService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listActivityApply_B")
    public R listActivityApply_B(@RequestParam Map<String, Object> param, ActivityApply activityApply) {
        Page<ActivityApply> page = MPUtil.getPage(param);
        IPage<ActivityApply> page1 = activityApplyService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), activityApply),
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
    public R query(ActivityApply activityApply) {
        QueryWrapper<ActivityApply> queryWrapper = new QueryWrapper<>();
        List<ActivityApply> list = activityApplyService.list(MPUtil.likeOrEq(queryWrapper, activityApply));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailActivityApply_B/{id}")
    public R detailActivityApply_B(@PathVariable("id") Long id) {
        ActivityApply obj = activityApplyService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailActivityApply_F/{id}")
    public R detailActivityApply_F(@PathVariable("id") Long id) {
        ActivityApply obj = activityApplyService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivityApply activityApply) {
            activityApplyService.save(activityApply);
        return R.ok("添加成功").put("data", activityApply);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ActivityApply activityApply) {
            activityApplyService.save(activityApply);
        return R.ok("添加成功").put("data", activityApply);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateActivityApply_B")
    public R updateActivityApply_B(@Valid @RequestBody List<ActivityApply> activityApplys) {
            activityApplyService.updateBatchById(activityApplys);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateActivityApply_F")
    public R updateActivityApply_F(@Valid @RequestBody ActivityApply activityApply) {
            activityApplyService.updateById(activityApply);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteActivityApply_B")
    public R deleteActivityApply_B(@RequestBody List<Long> ids) {
            activityApplyService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteActivityApply_F/{id}")
    public R deleteActivityApply_F(@PathVariable Long id) {
            activityApplyService.removeById(id);
        return R.ok();
    }
}