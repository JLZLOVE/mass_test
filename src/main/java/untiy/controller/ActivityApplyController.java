package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "活动申请管理", description = "活动申请相关接口")
@RestController
@RequestMapping("/activity-apply")
public class ActivityApplyController {

    @Autowired
    private ActivityApplyService activityApplyService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有活动申请列表", description = "返回全部活动申请记录，无分页参数")
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
    @Operation(summary = "前端公开查询活动申请", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询活动申请", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询活动申请", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(ActivityApply activityApply) {
        QueryWrapper<ActivityApply> queryWrapper = new QueryWrapper<>();
        List<ActivityApply> list = activityApplyService.list(MPUtil.likeOrEq(queryWrapper, activityApply));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询活动申请（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailActivityApply_B/{id}")
    public R detailActivityApply_B(@PathVariable("id") Long id) {
        ActivityApply obj = activityApplyService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询活动申请（公开）", description = "无需登录即可查看")
    @GetMapping("/detailActivityApply_F/{id}")
    public R detailActivityApply_F(@PathVariable("id") Long id) {
        ActivityApply obj = activityApplyService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增活动申请（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivityApply activityApply) {
        activityApplyService.save(activityApply);
        return R.ok("添加成功").put("data", activityApply);
    }



    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新活动申请（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateActivityApply_B")
    public R updateActivityApply_B(@Valid @RequestBody List<ActivityApply> activityApplys) {
        activityApplyService.updateBatchById(activityApplys);
        return R.ok();
    }



    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除活动申请（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteActivityApply_B")
    public R deleteActivityApply_B(@RequestBody List<Long> ids) {
        activityApplyService.removeByIds(ids);
        return R.ok();
    }


}