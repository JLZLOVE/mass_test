package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import untiy.annotation.RequiresLevel;
import untiy.entity.ActivityApply;
import untiy.entity.dto.*;
import untiy.exception.Level;
import untiy.service.ActivityApplyService;
import untiy.utils.ActivityFileStorageUtil;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Tag(name = "活动审批", description = "活动申请提交、审批、变更、取消与总结")
@RequestMapping("/activity-apply")
public class ActivityApplyController {

    @Autowired
    private ActivityApplyService activityApplyService;

    @Autowired
    private ActivityFileStorageUtil activityFileStorageUtil;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "提交活动申请", description = "部长/社长/指导老师/学院书记/管理员可发起；动态生成审批链")
    @PostMapping("/submit")
    public R submit(@Valid @RequestBody ActivitySubmitDTO dto) {
        activityApplyService.submit(dto);
        return R.ok("提交成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "上传申请附件", description = "本地存储，返回相对路径供 submit 使用")
    @PostMapping("/upload/attachment")
    public R uploadAttachment(@RequestParam("file") MultipartFile file) {
        String path = activityFileStorageUtil.store(file, "apply");
        return R.ok("上传成功").put("data", path);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "上传总结附件")
    @PostMapping("/upload/summary")
    public R uploadSummaryFile(@RequestParam("file") MultipartFile file) {
        String path = activityFileStorageUtil.store(file, "summary");
        return R.ok("上传成功").put("data", path);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "审批通过", description = "当前步骤审批人操作；指导老师可调整活动级别")
    @PostMapping("/approve/{id}")
    public R approve(@PathVariable Long id, @Valid @RequestBody ActivityApproveDTO dto) {
        activityApplyService.approve(id, dto);
        return R.ok("审批完成");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "审批驳回", description = "必须填写驳回原因")
    @PostMapping("/reject/{id}")
    public R reject(@PathVariable Long id, @Valid @RequestBody ActivityApproveDTO dto) {
        activityApplyService.reject(id, dto);
        return R.ok("已驳回");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "变更申请", description = "已通过活动可申请变更时间/地点")
    @PostMapping("/change/{id}")
    public R change(@PathVariable Long id, @Valid @RequestBody ActivityChangeDTO dto) {
        activityApplyService.requestChange(id, dto);
        return R.ok("变更申请已提交");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "取消活动", description = "仅申请人可直接取消")
    @PostMapping("/cancel/{id}")
    public R cancel(@PathVariable Long id, @Valid @RequestBody ActivityCancelDTO dto) {
        activityApplyService.cancel(id, dto);
        return R.ok("已取消");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "上传活动总结", description = "活动结束后1-3天内")
    @PostMapping("/summary/{id}")
    public R summary(@PathVariable Long id, @Valid @RequestBody ActivitySummaryDTO dto) {
        activityApplyService.uploadSummary(id, dto);
        return R.ok("总结已保存");
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "分页查询", description = "支持条件筛选与排序；Level 4 仅返回本人参与的活动")
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> param, ActivityApply query) {
        IPage<ActivityApply> page = activityApplyService.pageQuery(param, query);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "活动详情", description = "含审批流与变更历史")
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok().put("data", activityApplyService.getDetail(id));
    }
}
