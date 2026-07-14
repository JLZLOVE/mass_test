package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.ClubApplication;
import untiy.entity.ClubCategory;
import untiy.entity.dto.ClubAdminApproveDTO;
import untiy.entity.dto.ClubCollegeApproveDTO;
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

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "社团性质列表", description = "固定六类，创建申请 category 须从中选择")
    @GetMapping("/categories")
    public R categories() {
        return R.ok().put("data", ClubCategory.all());
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "发起创建申请", description = "指导老师提交社团创建申请；返回申请编号 applicationNo")
    @PostMapping("/apply/create")
    public R createApply(@Valid @RequestBody ClubCreateApplyDTO dto) {
        String applicationNo = clubApplicationService.createApply(dto);
        return R.ok("提交成功").put("data", applicationNo);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "发起解散申请", description = "社团指导老师提交解散申请")
    @PostMapping("/apply/dissolve")
    public R dissolveApply(@Valid @RequestBody ClubDissolveApplyDTO dto) {
        clubApplicationService.dissolveApply(dto);
        return R.ok("提交成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "分页查询申请列表", description = "username=申请人 username；支持状态、类型、时间范围筛选")
    @GetMapping("/apply/list")
    public R list(@RequestParam Map<String, Object> param,
                  @RequestParam(required = false) String username,
                  ClubApplication query) {
        IPage<ClubApplication> page = clubApplicationService.pageQuery(param, query, username);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "申请详情", description = "username=申请人 username")
    @GetMapping("/apply/detail")
    public R detail(@RequestParam(required = false) String username) {

        if (StringUtils.isNotBlank(username)) {
            return R.ok().put("data", clubApplicationService.getDetailByUsername(username.trim()));
        }
        return R.error("请提供 username ");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "学院审批", description = "body 传 username 或 applicationNo，及 approved、opinion")
    @PostMapping("/approve/college")
    public R approveCollege(@Valid @RequestBody ClubCollegeApproveDTO dto) {
        clubApplicationService.approveCollege(dto);
        return R.ok("审批完成");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "校级审批", description = "body 传 username（申请人）及 approved、opinion")
    @PostMapping("/approve/admin")
    public R approveAdmin(@Valid @RequestBody ClubAdminApproveDTO dto) {
        clubApplicationService.approveAdmin(dto);
        return R.ok("审批完成");
    }
}
