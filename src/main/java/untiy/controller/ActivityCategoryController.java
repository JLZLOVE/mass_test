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
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "活动分类管理", description = "活动分类相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/activity-category")
public class ActivityCategoryController {

    @Autowired
    private ActivityCategoryService activityCategoryService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有活动分类列表", description = "返回全部活动分类记录，无分页参数")
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
    @Operation(summary = "前端公开查询活动分类", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询活动分类", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询活动分类", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(ActivityCategory activityCategory) {
        QueryWrapper<ActivityCategory> queryWrapper = new QueryWrapper<>();
        List<ActivityCategory> list = activityCategoryService.list(MPUtil.likeOrEq(queryWrapper, activityCategory));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询活动分类（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailActivityCategory_B/{id}")
    public R detailActivityCategory_B(@PathVariable("id") Long id) {
        ActivityCategory obj = activityCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询活动分类（公开）", description = "无需登录即可查看")
    @GetMapping("/detailActivityCategory_F/{id}")
    public R detailActivityCategory_F(@PathVariable("id") Long id) {
        ActivityCategory obj = activityCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增活动分类（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivityCategory activityCategory) {
        activityCategoryService.save(activityCategory);
        return R.ok("添加成功").put("data", activityCategory);
    }

 /*   *//**
     * 前端增加（公开）
     *//*
    @IgnoreAuth
    @Operation(summary = "新增活动分类（公开）", description = "用户自行提交，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ActivityCategory activityCategory) {
        activityCategoryService.save(activityCategory);
        return R.ok("添加成功").put("data", activityCategory);
    }*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新活动分类（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateActivityCategory_B")
    public R updateActivityCategory_B(@Valid @RequestBody List<ActivityCategory> activityCategorys) {
        activityCategoryService.updateBatchById(activityCategorys);
        return R.ok();
    }

/*    *//**
     * 前端单个更新（公开）
     *//*
    @IgnoreAuth
    @Operation(summary = "更新单个活动分类（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateActivityCategory_F")
    public R updateActivityCategory_F(@Valid @RequestBody ActivityCategory activityCategory) {
        activityCategoryService.updateById(activityCategory);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除活动分类（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteActivityCategory_B")
    public R deleteActivityCategory_B(@RequestBody List<Long> ids) {
        activityCategoryService.removeByIds(ids);
        return R.ok();
    }

 /*   *//**
     * 前端单个删除（公开）
     *//*
    @Operation(summary = "删除单个活动分类（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteActivityCategory_F/{id}")
    public R deleteActivityCategory_F(@PathVariable Long id) {
        activityCategoryService.removeById(id);
        return R.ok();
    }*/
}