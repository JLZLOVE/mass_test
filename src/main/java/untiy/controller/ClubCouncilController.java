package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.dto.CouncilInitiateDTO;
import untiy.exception.Level;
import untiy.service.ClubCouncilService;
import untiy.utils.R;

import javax.validation.Valid;

@RestController
@Tag(name = "社团合议", description = "合议解散发起与签字")
@RequestMapping("/club-council")
public class ClubCouncilController {

    @Autowired
    private ClubCouncilService clubCouncilService;

    @RequiresLevel(minLevel = Level.SUPER_ADMIN)
    @Operation(summary = "发起合议解散", description = "超管发起；须在学院管理范围内")
    @PostMapping("/council/initiate")
    public R initiate(@Valid @RequestBody CouncilInitiateDTO dto) {
        clubCouncilService.initiate(dto);
        return R.ok("合议已发起");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "合议签字", description = "超管/校级管理员签字；达成条件后自动执行解散")
    @PostMapping("/council/sign/{id}")
    public R sign(@PathVariable Long id) {
        clubCouncilService.sign(id);
        return R.ok("签字成功");
    }
}
