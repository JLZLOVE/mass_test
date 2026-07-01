package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.NoticeReadRecord;
import untiy.service.NoticeReadRecordService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 通知阅读记录表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@Tag(name = "通知阅读记录管理", description = "通知阅读记录相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/notice-read-record")
public class NoticeReadRecordController {

    @Autowired
    private NoticeReadRecordService noticeReadRecordService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有通知阅读记录列表", description = "返回全部通知阅读记录，无分页参数")
    @GetMapping("/listNoticeReadRecord")
    public R listNoticeReadRecord() {
        QueryWrapper<NoticeReadRecord> ew = new QueryWrapper<>();
        List<NoticeReadRecord> list = noticeReadRecordService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询通知阅读记录", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listNoticeReadRecord_F")
    public R listNoticeReadRecord_F(@RequestParam Map<String, Object> param, NoticeReadRecord noticeReadRecord) {
        QueryWrapper<NoticeReadRecord> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, noticeReadRecord);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<NoticeReadRecord> page = MPUtil.getPage(param);
        IPage<NoticeReadRecord> page1 = noticeReadRecordService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询通知阅读记录", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listNoticeReadRecord_B")
    public R listNoticeReadRecord_B(@RequestParam Map<String, Object> param, NoticeReadRecord noticeReadRecord) {
        Page<NoticeReadRecord> page = MPUtil.getPage(param);
        IPage<NoticeReadRecord> page1 = noticeReadRecordService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), noticeReadRecord),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询通知阅读记录", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(NoticeReadRecord noticeReadRecord) {
        QueryWrapper<NoticeReadRecord> queryWrapper = new QueryWrapper<>();
        List<NoticeReadRecord> list = noticeReadRecordService.list(MPUtil.likeOrEq(queryWrapper, noticeReadRecord));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询通知阅读记录（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailNoticeReadRecord_B/{id}")
    public R detailNoticeReadRecord_B(@PathVariable("id") Long id) {
        NoticeReadRecord obj = noticeReadRecordService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询通知阅读记录（公开）", description = "无需登录即可查看")
    @GetMapping("/detailNoticeReadRecord_F/{id}")
    public R detailNoticeReadRecord_F(@PathVariable("id") Long id) {
        NoticeReadRecord obj = noticeReadRecordService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增通知阅读记录（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody NoticeReadRecord noticeReadRecord) {
        noticeReadRecordService.save(noticeReadRecord);
        return R.ok("添加成功").put("data", noticeReadRecord);
    }

    /**
     * 前端增加（公开）
     */
/*    @IgnoreAuth
    @Operation(summary = "新增通知阅读记录（公开）", description = "用户自行提交，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody NoticeReadRecord noticeReadRecord) {
        noticeReadRecordService.save(noticeReadRecord);
        return R.ok("添加成功").put("data", noticeReadRecord);
    }*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新通知阅读记录（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateNoticeReadRecord_B")
    public R updateNoticeReadRecord_B(@Valid @RequestBody List<NoticeReadRecord> noticeReadRecords) {
        noticeReadRecordService.updateBatchById(noticeReadRecords);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
 /*   @IgnoreAuth
    @Operation(summary = "更新单个通知阅读记录（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateNoticeReadRecord_F")
    public R updateNoticeReadRecord_F(@Valid @RequestBody NoticeReadRecord noticeReadRecord) {
        noticeReadRecordService.updateById(noticeReadRecord);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除通知阅读记录（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteNoticeReadRecord_B")
    public R deleteNoticeReadRecord_B(@RequestBody List<Long> ids) {
        noticeReadRecordService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
/*    @Operation(summary = "删除单个通知阅读记录（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteNoticeReadRecord_F/{id}")
    public R deleteNoticeReadRecord_F(@PathVariable Long id) {
        noticeReadRecordService.removeById(id);
        return R.ok();
    }*/
}