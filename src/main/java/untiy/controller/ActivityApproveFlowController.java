package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.ActivityApproveFlow;
import untiy.service.ActivityApproveFlowService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 活动审批流程表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
// 其他原有 import 保持不变...

@RestController
@Tag(name = "活动审批流程管理", description = "活动审批流程相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/activity-approve-flow")
public class ActivityApproveFlowController {

    @Autowired
    private ActivityApproveFlowService activityApproveFlowService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有审批流程列表", description = "返回全部审批流程记录，无分页参数")
    @GetMapping("/listActivityApproveFlow")
    public R listActivityApproveFlow() {
        QueryWrapper<ActivityApproveFlow> ew = new QueryWrapper<>();
        List<ActivityApproveFlow> list = activityApproveFlowService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询审批流程", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listActivityApproveFlow_F")
    public R listActivityApproveFlow_F(@RequestParam Map<String, Object> param, ActivityApproveFlow activityApproveFlow) {
        QueryWrapper<ActivityApproveFlow> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, activityApproveFlow);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<ActivityApproveFlow> page = MPUtil.getPage(param);
        IPage<ActivityApproveFlow> page1 = activityApproveFlowService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询审批流程", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listActivityApproveFlow_B")
    public R listActivityApproveFlow_B(@RequestParam Map<String, Object> param, ActivityApproveFlow activityApproveFlow) {
        Page<ActivityApproveFlow> page = MPUtil.getPage(param);
        IPage<ActivityApproveFlow> page1 = activityApproveFlowService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), activityApproveFlow),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询审批流程", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(ActivityApproveFlow activityApproveFlow) {
        QueryWrapper<ActivityApproveFlow> queryWrapper = new QueryWrapper<>();
        List<ActivityApproveFlow> list = activityApproveFlowService.list(MPUtil.likeOrEq(queryWrapper, activityApproveFlow));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询审批流程（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailActivityApproveFlow_B/{id}")
    public R detailActivityApproveFlow_B(@PathVariable("id") Long id) {
        ActivityApproveFlow obj = activityApproveFlowService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询审批流程（公开）", description = "无需登录即可查看")
    @GetMapping("/detailActivityApproveFlow_F/{id}")
    public R detailActivityApproveFlow_F(@PathVariable("id") Long id) {
        ActivityApproveFlow obj = activityApproveFlowService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增审批流程（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ActivityApproveFlow activityApproveFlow) {
        activityApproveFlowService.save(activityApproveFlow);
        return R.ok("添加成功").put("data", activityApproveFlow);
    }

    /*   *//**
     * 前端增加（公开）
     *//*
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ActivityApproveFlow activityApproveFlow) {
            activityApproveFlowService.save(activityApproveFlow);
        return R.ok("添加成功").put("data", activityApproveFlow);
    }
*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新审批流程（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateActivityApproveFlow_B")
    public R updateActivityApproveFlow_B(@Valid @RequestBody List<ActivityApproveFlow> activityApproveFlows) {
        activityApproveFlowService.updateBatchById(activityApproveFlows);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
/*
    @IgnoreAuth
    @PutMapping("/updateActivityApproveFlow_F")
    public R updateActivityApproveFlow_F(@Valid @RequestBody ActivityApproveFlow activityApproveFlow) {
            activityApproveFlowService.updateById(activityApproveFlow);
        return R.ok();
    }
*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除审批流程（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteActivityApproveFlow_B")
    public R deleteActivityApproveFlow_B(@RequestBody List<Long> ids) {
        activityApproveFlowService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
/*    @DeleteMapping("/deleteActivityApproveFlow_F/{id}")
    public R deleteActivityApproveFlow_F(@PathVariable Long id) {
            activityApproveFlowService.removeById(id);
        return R.ok();
    }*/
}