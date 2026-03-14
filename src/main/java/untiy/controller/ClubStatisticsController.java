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
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/club-statistics")
public class ClubStatisticsController {

    @Autowired
    private ClubStatisticsService clubStatisticsService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(ClubStatistics clubStatistics) {
        QueryWrapper<ClubStatistics> queryWrapper = new QueryWrapper<>();
        List<ClubStatistics> list = clubStatisticsService.list(MPUtil.likeOrEq(queryWrapper, clubStatistics));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailClubStatistics_B/{id}")
    public R detailClubStatistics_B(@PathVariable("id") Long id) {
        ClubStatistics obj = clubStatisticsService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailClubStatistics_F/{id}")
    public R detailClubStatistics_F(@PathVariable("id") Long id) {
        ClubStatistics obj = clubStatisticsService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody ClubStatistics clubStatistics) {
            clubStatisticsService.save(clubStatistics);
        return R.ok("添加成功").put("data", clubStatistics);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody ClubStatistics clubStatistics) {
            clubStatisticsService.save(clubStatistics);
        return R.ok("添加成功").put("data", clubStatistics);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateClubStatistics_B")
    public R updateClubStatistics_B(@Valid @RequestBody List<ClubStatistics> clubStatisticss) {
            clubStatisticsService.updateBatchById(clubStatisticss);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateClubStatistics_F")
    public R updateClubStatistics_F(@Valid @RequestBody ClubStatistics clubStatistics) {
            clubStatisticsService.updateById(clubStatistics);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteClubStatistics_B")
    public R deleteClubStatistics_B(@RequestBody List<Long> ids) {
            clubStatisticsService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteClubStatistics_F/{id}")
    public R deleteClubStatistics_F(@PathVariable Long id) {
            clubStatisticsService.removeById(id);
        return R.ok();
    }
}