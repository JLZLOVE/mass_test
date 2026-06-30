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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "活动签到管理", description = "活动签到相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/activity-sign")
public class ActivitySignController {

    @Autowired
    private ActivitySignService activitySignService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有签到记录列表", description = "返回全部签到记录，无分页参数")
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
    @Operation(summary = "前端公开查询签到记录", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询签到记录", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询签到记录", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(ActivitySign activitySign) {
        QueryWrapper<ActivitySign> queryWrapper = new QueryWrapper<>();
        List<ActivitySign> list = activitySignService.list(MPUtil.likeOrEq(queryWrapper, activitySign));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询签到记录（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailActivitySign_B/{id}")
    public R detailActivitySign_B(@PathVariable("id") Long id) {
        ActivitySign obj = activitySignService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询签到记录（公开）", description = "无需登录即可查看")
    @GetMapping("/detailActivitySign_F/{id}")
    public R detailActivitySign_F(@PathVariable("id") Long id) {
        ActivitySign obj = activitySignService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增签到记录（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivitySign activitySign) {
        activitySignService.save(activitySign);
        return R.ok("添加成功").put("data", activitySign);
    }

    /**
     * 前端增加（公开）
     */
/*    @IgnoreAuth
    @Operation(summary = "新增签到记录（公开）", description = "用户自行提交签到，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ActivitySign activitySign) {
        activitySignService.save(activitySign);
        return R.ok("添加成功").put("data", activitySign);
    }*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新签到记录（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateActivitySign_B")
    public R updateActivitySign_B(@Valid @RequestBody List<ActivitySign> activitySigns) {
        activitySignService.updateBatchById(activitySigns);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
   /* @IgnoreAuth
    @Operation(summary = "更新单个签到记录（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateActivitySign_F")
    public R updateActivitySign_F(@Valid @RequestBody ActivitySign activitySign) {
        activitySignService.updateById(activitySign);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除签到记录（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteActivitySign_B")
    public R deleteActivitySign_B(@RequestBody List<Long> ids) {
        activitySignService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
  /*  @Operation(summary = "删除单个签到记录（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteActivitySign_F/{id}")
    public R deleteActivitySign_F(@PathVariable Long id) {
        activitySignService.removeById(id);
        return R.ok();
    }*/
}