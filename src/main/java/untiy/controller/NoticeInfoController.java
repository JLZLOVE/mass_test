package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.NoticeInfo;
import untiy.service.NoticeInfoService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 通知表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */



@RestController
@Tag(name = "公告信息管理", description = "公告信息相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/notice-info")
public class NoticeInfoController {

    @Autowired
    private NoticeInfoService noticeInfoService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有公告信息列表", description = "返回全部公告信息记录，无分页参数")
    @GetMapping("/listNoticeInfo")
    public R listNoticeInfo() {
        QueryWrapper<NoticeInfo> ew = new QueryWrapper<>();
        List<NoticeInfo> list = noticeInfoService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询公告信息", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listNoticeInfo_F")
    public R listNoticeInfo_F(@RequestParam Map<String, Object> param, NoticeInfo noticeInfo) {
        QueryWrapper<NoticeInfo> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, noticeInfo);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<NoticeInfo> page = MPUtil.getPage(param);
        IPage<NoticeInfo> page1 = noticeInfoService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询公告信息", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listNoticeInfo_B")
    public R listNoticeInfo_B(@RequestParam Map<String, Object> param, NoticeInfo noticeInfo) {
        Page<NoticeInfo> page = MPUtil.getPage(param);
        IPage<NoticeInfo> page1 = noticeInfoService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), noticeInfo),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询公告信息", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(NoticeInfo noticeInfo) {
        QueryWrapper<NoticeInfo> queryWrapper = new QueryWrapper<>();
        List<NoticeInfo> list = noticeInfoService.list(MPUtil.likeOrEq(queryWrapper, noticeInfo));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询公告信息（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailNoticeInfo_B/{id}")
    public R detailNoticeInfo_B(@PathVariable("id") Long id) {
        NoticeInfo obj = noticeInfoService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询公告信息（公开）", description = "无需登录即可查看")
    @GetMapping("/detailNoticeInfo_F/{id}")
    public R detailNoticeInfo_F(@PathVariable("id") Long id) {
        NoticeInfo obj = noticeInfoService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增公告信息（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody NoticeInfo noticeInfo) {
        noticeInfoService.save(noticeInfo);
        return R.ok("添加成功").put("data", noticeInfo);
    }

    /**
     * 前端增加（公开）
     */
 /*   @IgnoreAuth
    @Operation(summary = "新增公告信息（公开）", description = "用户自行提交，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody NoticeInfo noticeInfo) {
        noticeInfoService.save(noticeInfo);
        return R.ok("添加成功").put("data", noticeInfo);
    }*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新公告信息（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateNoticeInfo_B")
    public R updateNoticeInfo_B(@Valid @RequestBody List<NoticeInfo> noticeInfos) {
        noticeInfoService.updateBatchById(noticeInfos);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
/*    @IgnoreAuth
    @Operation(summary = "更新单个公告信息（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateNoticeInfo_F")
    public R updateNoticeInfo_F(@Valid @RequestBody NoticeInfo noticeInfo) {
        noticeInfoService.updateById(noticeInfo);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除公告信息（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteNoticeInfo_B")
    public R deleteNoticeInfo_B(@RequestBody List<Long> ids) {
        noticeInfoService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
 /*   @Operation(summary = "删除单个公告信息（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteNoticeInfo_F/{id}")
    public R deleteNoticeInfo_F(@PathVariable Long id) {
        noticeInfoService.removeById(id);
        return R.ok();
    }*/
}