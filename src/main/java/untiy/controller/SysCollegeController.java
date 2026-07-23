package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.annotation.RequiresLevel;
import untiy.entity.SysCollege;
import untiy.exception.Level;
import untiy.service.SysCollegeService;
import untiy.utils.R;

import java.util.List;

@RestController
@Tag(name = "学院", description = "学院下拉列表")
@RequestMapping("/sys-college")
public class SysCollegeController {

    @Autowired
    private SysCollegeService sysCollegeService;

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "学院列表", description = "按权限过滤；keyword 模糊搜索学院名称")
    @GetMapping("/list")
    public R list(@RequestParam(required = false) String keyword) {
        List<SysCollege> list = sysCollegeService.listForCurrentUser(keyword);
        return R.ok().put("data", list);
    }
}
