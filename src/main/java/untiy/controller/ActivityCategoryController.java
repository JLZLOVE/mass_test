package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.ActivityCategory;
import untiy.service.ActivityCategoryService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 活动分类表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/activity-category")
public class ActivityCategoryController {

    @Autowired
    private ActivityCategoryService activityCategoryService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listActivityCategory")
    public R listActivityCategory() {
        QueryWrapper<ActivityCategory> ew = new QueryWrapper<>();
        List<ActivityCategory> list = activityCategoryService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listActivityCategory_F")
    public R listActivityCategory_F(@RequestParam Map<String, Object> param, ActivityCategory activityCategory) {
        QueryWrapper<ActivityCategory> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, activityCategory);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<ActivityCategory> page = MPUtil.getPage(param);             
        IPage<ActivityCategory> page1 = activityCategoryService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listActivityCategory_B")
    public R listActivityCategory_B(@RequestParam Map<String, Object> param, ActivityCategory activityCategory) {
        Page<ActivityCategory> page = MPUtil.getPage(param);
        IPage<ActivityCategory> page1 = activityCategoryService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), activityCategory),
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
    public R query(ActivityCategory activityCategory) {
        QueryWrapper<ActivityCategory> queryWrapper = new QueryWrapper<>();
        List<ActivityCategory> list = activityCategoryService.list(MPUtil.likeOrEq(queryWrapper, activityCategory));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailActivityCategory_B/{id}")
    public R detailActivityCategory_B(@PathVariable("id") Long id) {
        ActivityCategory obj = activityCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailActivityCategory_F/{id}")
    public R detailActivityCategory_F(@PathVariable("id") Long id) {
        ActivityCategory obj = activityCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivityCategory activityCategory) {
            activityCategoryService.save(activityCategory);
        return R.ok("添加成功").put("data", activityCategory);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ActivityCategory activityCategory) {
            activityCategoryService.save(activityCategory);
        return R.ok("添加成功").put("data", activityCategory);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateActivityCategory_B")
    public R updateActivityCategory_B(@Valid @RequestBody List<ActivityCategory> activityCategorys) {
            activityCategoryService.updateBatchById(activityCategorys);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateActivityCategory_F")
    public R updateActivityCategory_F(@Valid @RequestBody ActivityCategory activityCategory) {
            activityCategoryService.updateById(activityCategory);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteActivityCategory_B")
    public R deleteActivityCategory_B(@RequestBody List<Long> ids) {
            activityCategoryService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteActivityCategory_F/{id}")
    public R deleteActivityCategory_F(@PathVariable Long id) {
            activityCategoryService.removeById(id);
        return R.ok();
    }
}