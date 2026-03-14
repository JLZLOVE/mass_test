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
@RequestMapping("/notice-info")
public class NoticeInfoController {

    @Autowired
    private NoticeInfoService noticeInfoService;

    /**
     * 列表查询（后端）
     */
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
    @GetMapping("/query")
    public R query(NoticeInfo noticeInfo) {
        QueryWrapper<NoticeInfo> queryWrapper = new QueryWrapper<>();
        List<NoticeInfo> list = noticeInfoService.list(MPUtil.likeOrEq(queryWrapper, noticeInfo));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailNoticeInfo_B/{id}")
    public R detailNoticeInfo_B(@PathVariable("id") Long id) {
        NoticeInfo obj = noticeInfoService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailNoticeInfo_F/{id}")
    public R detailNoticeInfo_F(@PathVariable("id") Long id) {
        NoticeInfo obj = noticeInfoService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody NoticeInfo noticeInfo) {
            noticeInfoService.save(noticeInfo);
        return R.ok("添加成功").put("data", noticeInfo);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody NoticeInfo noticeInfo) {
            noticeInfoService.save(noticeInfo);
        return R.ok("添加成功").put("data", noticeInfo);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateNoticeInfo_B")
    public R updateNoticeInfo_B(@Valid @RequestBody List<NoticeInfo> noticeInfos) {
            noticeInfoService.updateBatchById(noticeInfos);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateNoticeInfo_F")
    public R updateNoticeInfo_F(@Valid @RequestBody NoticeInfo noticeInfo) {
            noticeInfoService.updateById(noticeInfo);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteNoticeInfo_B")
    public R deleteNoticeInfo_B(@RequestBody List<Long> ids) {
            noticeInfoService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteNoticeInfo_F/{id}")
    public R deleteNoticeInfo_F(@PathVariable Long id) {
            noticeInfoService.removeById(id);
        return R.ok();
    }
}