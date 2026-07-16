package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import untiy.annotation.RequiresLevel;
import untiy.entity.ActivitySignConfig;
import untiy.entity.dto.*;
import untiy.entity.vo.SignRecordVO;
import untiy.entity.vo.SignStatsVO;
import untiy.exception.Level;
import untiy.service.ActivitySignService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Tag(name = "活动签到", description = "签到配置、签到签退、补签与统计")
@RequestMapping("/activity-sign")
public class ActivitySignController {

    @Autowired
    private ActivitySignService activitySignService;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "配置签到", description = "活动负责人配置签到规则")
    @PostMapping("/config")
    public R saveConfig(@Valid @RequestBody SignConfigDTO dto) {
        activitySignService.saveConfig(dto);
        return R.ok("配置成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "更新签到配置", description = "活动负责人或指导老师")
    @PutMapping("/config")
    public R updateConfig(@Valid @RequestBody SignConfigDTO dto) {
        activitySignService.updateConfig(dto);
        return R.ok("更新成功");
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "查询签到配置", description = "参与人查看签到点信息")
    @GetMapping("/config")
    public R getConfig(@RequestParam String activityNo) {
        ActivitySignConfig config = activitySignService.getConfig(activityNo);
        return R.ok().put("data", config);
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "签到", description = "定位或扫码签到")
    @PostMapping("/sign")
    public R sign(@RequestParam String activityNo, @RequestBody SignActionDTO dto) {
        activitySignService.sign(activityNo, dto);
        return R.ok("签到成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "手动签到", description = "管理员后台标记签到")
    @PostMapping("/admin/sign")
    public R adminSign(@RequestParam String activityNo, @Valid @RequestBody AdminSignDTO dto) {
        activitySignService.adminSign(activityNo, dto);
        return R.ok("签到成功");
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "签退")
    @PostMapping("/checkout")
    public R checkout(@RequestParam String activityNo) {
        activitySignService.checkout(activityNo);
        return R.ok("签退成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "申请补签", description = "社长为成员发起")
    @PostMapping("/apply")
    public R applyMakeup(@RequestParam String activityNo, @Valid @RequestBody MakeupApplyDTO dto) {
        Long id = activitySignService.applyMakeup(activityNo, dto);
        return R.ok("补签申请已提交").put("data", id);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "补签审批")
    @PostMapping("/approve/{applyId}")
    public R approveMakeup(@PathVariable Long applyId, @Valid @RequestBody MakeupApproveDTO dto) {
        activitySignService.approveMakeup(applyId, dto);
        return R.ok("审批完成");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "签到统计")
    @GetMapping("/stats")
    public R stats(@RequestParam String activityNo) {
        SignStatsVO stats = activitySignService.stats(activityNo);
        return R.ok().put("data", stats);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "签到明细列表")
    @GetMapping("/list")
    public R list(@RequestParam String activityNo, @RequestParam(required = false) Map<String, Object> param) {
        if (param != null) {
            param.remove("activityNo");
        }
        IPage<SignRecordVO> page = activitySignService.listRecords(activityNo, param);
        return R.ok().put("data", page);
    }
}
