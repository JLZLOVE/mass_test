package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import untiy.annotation.RequiresLevel;
import untiy.entity.SysClub;
import untiy.entity.SysDepartment;
import untiy.entity.vo.ClubMemberCountVO;
import untiy.entity.vo.SysClubListVO;
import untiy.entity.vo.SysUserRoleVO;
import untiy.exception.Level;
import untiy.service.SysClubService;
import untiy.utils.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "社团管理", description = "管理端社团列表、详情、成员与部门")
@RequestMapping("/sys-club")
public class SysClubController {

    @Autowired
    private SysClubService sysClubService;

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "管理端社团分页列表",
            description = "tabMode=normal|dissolving|council；keyword 搜名称/编号；返回 canDissolve/canSignCouncil")
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> param,
                  SysClub query,
                  @RequestParam(required = false) String keyword,
                  @RequestParam(required = false, defaultValue = "normal") String tabMode) {
        IPage<SysClubListVO> page = sysClubService.adminPageQuery(param, query, keyword, tabMode);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "社团详情", description = "按 clubCode 查询")
    @GetMapping("/detail/{clubCode}")
    public R detail(@PathVariable String clubCode) {
        return R.ok().put("data", sysClubService.getDetailByClubCode(clubCode));
    }

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "社团部门列表")
    @GetMapping("/departments/{clubCode}")
    public R departments(@PathVariable String clubCode) {
        List<SysDepartment> list = sysClubService.listDepartments(clubCode);
        return R.ok().put("data", list);
    }

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "社团成员分页")
    @GetMapping("/members/{clubCode}")
    public R members(@RequestParam Map<String, Object> param,
                     @PathVariable String clubCode,
                     @RequestParam(required = false) String roleCode) {
        IPage<SysUserRoleVO> page = sysClubService.pageMembers(param, clubCode, roleCode);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "批量成员数", description = "clubIds 逗号分隔，返回 {clubId, memberCount}")
    @GetMapping("/member-count")
    public R memberCount(@RequestParam String clubIds) {
        List<Long> ids = parseIds(clubIds);
        List<ClubMemberCountVO> list = sysClubService.batchMemberCount(ids);
        return R.ok().put("data", list);
    }

    private List<Long> parseIds(String clubIds) {
        if (clubIds == null || clubIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(clubIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
