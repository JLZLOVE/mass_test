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
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "公告分类管理", description = "公告分类相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/notice-category")
public class NoticeCategoryController {

    @Autowired
    private NoticeCategoryService noticeCategoryService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有公告分类列表", description = "返回全部公告分类记录，无分页参数")
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
    @Operation(summary = "前端公开查询公告分类", description = "支持多条件模糊匹配、时间范围、排序、分页")
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
    @Operation(summary = "后端鉴权查询公告分类", description = "支持分页、条件筛选、排序，仅管理员可用")
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
    @Operation(summary = "公开条件查询公告分类", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(NoticeCategory noticeCategory) {
        QueryWrapper<NoticeCategory> queryWrapper = new QueryWrapper<>();
        List<NoticeCategory> list = noticeCategoryService.list(MPUtil.likeOrEq(queryWrapper, noticeCategory));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询公告分类（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailNoticeCategory_B/{id}")
    public R detailNoticeCategory_B(@PathVariable("id") Long id) {
        NoticeCategory obj = noticeCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询公告分类（公开）", description = "无需登录即可查看")
    @GetMapping("/detailNoticeCategory_F/{id}")
    public R detailNoticeCategory_F(@PathVariable("id") Long id) {
        NoticeCategory obj = noticeCategoryService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增公告分类（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody NoticeCategory noticeCategory) {
        noticeCategoryService.save(noticeCategory);
        return R.ok("添加成功").put("data", noticeCategory);
    }

    /**
     * 前端增加（公开）
     */
    @IgnoreAuth
    @Operation(summary = "新增公告分类（公开）", description = "用户自行提交，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody NoticeCategory noticeCategory) {
        noticeCategoryService.save(noticeCategory);
        return R.ok("添加成功").put("data", noticeCategory);
    }

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新公告分类（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateNoticeCategory_B")
    public R updateNoticeCategory_B(@Valid @RequestBody List<NoticeCategory> noticeCategorys) {
        noticeCategoryService.updateBatchById(noticeCategorys);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
 /*   @IgnoreAuth
    @Operation(summary = "更新单个公告分类（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateNoticeCategory_F")
    public R updateNoticeCategory_F(@Valid @RequestBody NoticeCategory noticeCategory) {
        noticeCategoryService.updateById(noticeCategory);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除公告分类（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteNoticeCategory_B")
    public R deleteNoticeCategory_B(@RequestBody List<Long> ids) {
        noticeCategoryService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
/*    @Operation(summary = "删除单个公告分类（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteNoticeCategory_F/{id}")
    public R deleteNoticeCategory_F(@PathVariable Long id) {
        noticeCategoryService.removeById(id);
        return R.ok();
    }*/
}