package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.ClubApplication;
import untiy.entity.dto.ClubApproveDTO;
import untiy.entity.dto.ClubCreateApplyDTO;
import untiy.entity.dto.ClubDissolveApplyDTO;
import untiy.exception.Level;
import untiy.service.ClubApplicationService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Tag(name = "社团申请", description = "社团创建/解散申请与审批")
@RequestMapping("/club-application")
public class ClubApplicationController {

    @Autowired
    private ClubApplicationService clubApplicationService;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "发起创建申请", description = "指导老师提交社团创建申请")
    @PostMapping("/apply/create")
    public R createApply(@Valid @RequestBody ClubCreateApplyDTO dto) {
        clubApplicationService.createApply(dto);
        return R.ok("提交成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "发起解散申请", description = "社团指导老师提交解散申请")
    @PostMapping("/apply/dissolve")
    public R dissolveApply(@Valid @RequestBody ClubDissolveApplyDTO dto) {
        clubApplicationService.dissolveApply(dto);
        return R.ok("提交成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "分页查询申请列表", description = "支持申请人、状态、类型、时间范围筛选")
    @GetMapping("/apply/list")
    public R list(@RequestParam Map<String, Object> param, ClubApplication query) {
        IPage<ClubApplication> page = clubApplicationService.pageQuery(param, query);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "申请详情", description = "含当前待审批人信息")
    @GetMapping("/apply/detail/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok().put("data", clubApplicationService.getDetail(id));
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "学院审批", description = "学院院长审批；入参 approved、opinion")
    @PostMapping("/approve/college/{id}")
    public R approveCollege(@PathVariable Long id, @Valid @RequestBody ClubApproveDTO dto) {
        clubApplicationService.approveCollege(id, dto);
        return R.ok("审批完成");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "校级审批", description = "校级管理员审批；创建通过激活社团，解散通过执行解散")
    @PostMapping("/approve/admin/{id}")
    public R approveAdmin(@PathVariable Long id, @Valid @RequestBody ClubApproveDTO dto) {
        clubApplicationService.approveAdmin(id, dto);
        return R.ok("审批完成");
    }
}
