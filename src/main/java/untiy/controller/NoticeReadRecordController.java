package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.NoticeReadRecord;
import untiy.service.NoticeReadRecordService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

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
@RequestMapping("/notice-read-record")
public class NoticeReadRecordController {

    @Autowired
    private NoticeReadRecordService noticeReadRecordService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(NoticeReadRecord noticeReadRecord) {
        QueryWrapper<NoticeReadRecord> queryWrapper = new QueryWrapper<>();
        List<NoticeReadRecord> list = noticeReadRecordService.list(MPUtil.likeOrEq(queryWrapper, noticeReadRecord));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailNoticeReadRecord_B/{id}")
    public R detailNoticeReadRecord_B(@PathVariable("id") Long id) {
        NoticeReadRecord obj = noticeReadRecordService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailNoticeReadRecord_F/{id}")
    public R detailNoticeReadRecord_F(@PathVariable("id") Long id) {
        NoticeReadRecord obj = noticeReadRecordService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody NoticeReadRecord noticeReadRecord) {
            noticeReadRecordService.save(noticeReadRecord);
        return R.ok("添加成功").put("data", noticeReadRecord);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody NoticeReadRecord noticeReadRecord) {
            noticeReadRecordService.save(noticeReadRecord);
        return R.ok("添加成功").put("data", noticeReadRecord);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateNoticeReadRecord_B")
    public R updateNoticeReadRecord_B(@Valid @RequestBody List<NoticeReadRecord> noticeReadRecords) {
            noticeReadRecordService.updateBatchById(noticeReadRecords);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateNoticeReadRecord_F")
    public R updateNoticeReadRecord_F(@Valid @RequestBody NoticeReadRecord noticeReadRecord) {
            noticeReadRecordService.updateById(noticeReadRecord);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteNoticeReadRecord_B")
    public R deleteNoticeReadRecord_B(@RequestBody List<Long> ids) {
            noticeReadRecordService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteNoticeReadRecord_F/{id}")
    public R deleteNoticeReadRecord_F(@PathVariable Long id) {
            noticeReadRecordService.removeById(id);
        return R.ok();
    }
}