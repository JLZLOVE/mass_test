package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.annotation.RequiresLevel;
import untiy.entity.vo.ClubMemberCountVO;
import untiy.exception.Level;
import untiy.service.SysClubService;
import untiy.utils.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "社团统计", description = "成员数等批量聚合")
@RequestMapping("/club-statistics")
public class ClubStatisticsController {

    @Autowired
    private SysClubService sysClubService;

    @RequiresLevel(minLevel = Level.DEPT_LEADER)
    @Operation(summary = "批量成员数", description = "请求参数 clubIds 逗号分隔，返回 { clubId, memberCount }")
    @GetMapping("/list")
    public R list(@RequestParam(required = false) String clubIds) {
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
