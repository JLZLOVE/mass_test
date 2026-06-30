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
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

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
@RestController
@RequestMapping("/activity-approve-flow")
public class ActivityApproveFlowController {

    @Autowired
    private ActivityApproveFlowService activityApproveFlowService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(ActivityApproveFlow activityApproveFlow) {
        QueryWrapper<ActivityApproveFlow> queryWrapper = new QueryWrapper<>();
        List<ActivityApproveFlow> list = activityApproveFlowService.list(MPUtil.likeOrEq(queryWrapper, activityApproveFlow));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailActivityApproveFlow_B/{id}")
    public R detailActivityApproveFlow_B(@PathVariable("id") Long id) {
        ActivityApproveFlow obj = activityApproveFlowService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailActivityApproveFlow_F/{id}")
    public R detailActivityApproveFlow_F(@PathVariable("id") Long id) {
        ActivityApproveFlow obj = activityApproveFlowService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
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