package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.ClubStatistics;
import untiy.service.ClubStatisticsService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 社团统计表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */


@RestController
@Tag(name = "社团统计管理", description = "社团统计相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/club-statistics")
public class ClubStatisticsController {

    @Autowired
    private ClubStatisticsService clubStatisticsService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有社团统计列表", description = "返回全部社团统计记录，无分页参数")
    @GetMapping("/listClubStatistics")
    public R listClubStatistics() {
        QueryWrapper<ClubStatistics> ew = new QueryWrapper<>();
        List<ClubStatistics> list = clubStatisticsService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询社团统计", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listClubStatistics_F")
    public R listClubStatistics_F(@RequestParam Map<String, Object> param, ClubStatistics clubStatistics) {
        QueryWrapper<ClubStatistics> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, clubStatistics);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<ClubStatistics> page = MPUtil.getPage(param);
        IPage<ClubStatistics> page1 = clubStatisticsService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询社团统计", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listClubStatistics_B")
    public R listClubStatistics_B(@RequestParam Map<String, Object> param, ClubStatistics clubStatistics) {
        Page<ClubStatistics> page = MPUtil.getPage(param);
        IPage<ClubStatistics> page1 = clubStatisticsService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), clubStatistics),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询社团统计", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(ClubStatistics clubStatistics) {
        QueryWrapper<ClubStatistics> queryWrapper = new QueryWrapper<>();
        List<ClubStatistics> list = clubStatisticsService.list(MPUtil.likeOrEq(queryWrapper, clubStatistics));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询社团统计（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailClubStatistics_B/{id}")
    public R detailClubStatistics_B(@PathVariable("id") Long id) {
        ClubStatistics obj = clubStatisticsService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询社团统计（公开）", description = "无需登录即可查看")
    @GetMapping("/detailClubStatistics_F/{id}")
    public R detailClubStatistics_F(@PathVariable("id") Long id) {
        ClubStatistics obj = clubStatisticsService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增社团统计（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ClubStatistics clubStatistics) {
        clubStatisticsService.save(clubStatistics);
        return R.ok("添加成功").put("data", clubStatistics);
    }



    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新社团统计（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateClubStatistics_B")
    public R updateClubStatistics_B(@Valid @RequestBody List<ClubStatistics> clubStatisticss) {
        clubStatisticsService.updateBatchById(clubStatisticss);
        return R.ok();
    }



    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除社团统计（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteClubStatistics_B")
    public R deleteClubStatistics_B(@RequestBody List<Long> ids) {
        clubStatisticsService.removeByIds(ids);
        return R.ok();
    }


}