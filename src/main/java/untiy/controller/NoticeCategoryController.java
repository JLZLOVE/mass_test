package untiy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.NoticeCategory;
import untiy.service.NoticeCategoryService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotion.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 通知分类表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/notice-category")
public class NoticeCategoryController {

    @Autowired
    private NoticeCategoryService noticeCategoryService;

    /**
     * 列表查询（后端）
     */
    @GetMapping("/listNoticeCategory")
    public R listNoticeCategory() {
        QueryWrapper<NoticeCategory> ew = new QueryWrapper<>();
        List<NoticeCategory> list = noticeCategoryService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/listNoticeCategory_F")
    public R listNoticeCategory_F(@RequestParam Map<String, Object> param, NoticeCategory noticeCategory) {
        QueryWrapper<NoticeCategory> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, noticeCategory);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<NoticeCategory> page = MPUtil.getPage(param);             
        IPage<NoticeCategory> page1 = noticeCategoryService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @GetMapping("/listNoticeCategory_B")
    public R listNoticeCategory_B(@RequestParam Map<String, Object> param, NoticeCategory noticeCategory) {
        Page<NoticeCategory> page = MPUtil.getPage(param);
        IPage<NoticeCategory> page1 = noticeCategoryService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), noticeCategory),
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
    public R query(NoticeCategory noticeCategory) {
        QueryWrapper<NoticeCategory> queryWrapper = new QueryWrapper<>();
        List<NoticeCategory> list = noticeCategoryService.list(MPUtil.likeOrEq(queryWrapper, noticeCategory));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @GetMapping("/detailNoticeCategory_B/{id}")
    public R detailNoticeCategory_B(@PathVariable("id") Long id) {
        NoticeCategory obj = noticeCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @GetMapping("/detailNoticeCategory_F/{id}")
    public R detailNoticeCategory_F(@PathVariable("id") Long id) {
        NoticeCategory obj = noticeCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody NoticeCategory noticeCategory) {
            noticeCategoryService.save(noticeCategory);
        return R.ok("添加成功").put("data", noticeCategory);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody NoticeCategory noticeCategory) {
            noticeCategoryService.save(noticeCategory);
        return R.ok("添加成功").put("data", noticeCategory);
    }

    /**
     * 后端批量更新
     */
    @PutMapping("/updateNoticeCategory_B")
    public R updateNoticeCategory_B(@Valid @RequestBody List<NoticeCategory> noticeCategorys) {
            noticeCategoryService.updateBatchById(noticeCategorys);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
    @IgnoreAuth
    @PutMapping("/updateNoticeCategory_F")
    public R updateNoticeCategory_F(@Valid @RequestBody NoticeCategory noticeCategory) {
            noticeCategoryService.updateById(noticeCategory);
        return R.ok();
    }

    /**
     * 后端批量删除
     */
    @DeleteMapping("/deleteNoticeCategory_B")
    public R deleteNoticeCategory_B(@RequestBody List<Long> ids) {
            noticeCategoryService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
    @DeleteMapping("/deleteNoticeCategory_F/{id}")
    public R deleteNoticeCategory_F(@PathVariable Long id) {
            noticeCategoryService.removeById(id);
        return R.ok();
    }
}