package untiy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysMajor;
import untiy.service.SysMajorService;
import untiy.utils.MPUtil;
import untiy.utils.R;
import untiy.annotation.IgnoreAuth;   // 注意：这里是 annotion，不是 annotation

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 专业表 前端控制器
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@RestController
@Tag(name = "专业管理", description = "专业相关接口，包含查询、新增、更新、删除等操作")
@RequestMapping("/sys-major")
public class SysMajorController {

    @Autowired
    private SysMajorService sysMajorService;

    /**
     * 列表查询（后端）
     */
    @Operation(summary = "查询所有专业列表", description = "返回全部专业记录，无分页参数")
    @GetMapping("/listSysMajor")
    public R listSysMajor() {
        QueryWrapper<SysMajor> ew = new QueryWrapper<>();
        List<SysMajor> list = sysMajorService.list(ew);
        return R.ok().put("data", list);
    }

    /**
     * 前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "前端公开查询专业", description = "支持多条件模糊匹配、时间范围、排序、分页")
    @GetMapping("/listSysMajor_F")
    public R listSysMajor_F(@RequestParam Map<String, Object> param, SysMajor sysMajor) {
        QueryWrapper<SysMajor> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysMajor);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysMajor> page = MPUtil.getPage(param);
        IPage<SysMajor> page1 = sysMajorService.page(page, queryWrapper);
        return R.ok().put("data", page1);
    }

    /**
     * 后端查询（需鉴权）
     */
    @Operation(summary = "后端鉴权查询专业", description = "支持分页、条件筛选、排序，仅管理员可用")
    @GetMapping("/listSysMajor_B")
    public R listSysMajor_B(@RequestParam Map<String, Object> param, SysMajor sysMajor) {
        Page<SysMajor> page = MPUtil.getPage(param);
        IPage<SysMajor> page1 = sysMajorService.page(page, MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysMajor),
                        param
                ),
                param
        ));
        return R.ok().put("data", page1);
    }

    /**
     * 公开条件查询
     */
    @Operation(summary = "公开条件查询专业", description = "根据实体字段模糊匹配，返回所有符合条件的记录（不分页）")
    @GetMapping("/query")
    public R query(SysMajor sysMajor) {
        QueryWrapper<SysMajor> queryWrapper = new QueryWrapper<>();
        List<SysMajor> list = sysMajorService.list(MPUtil.likeOrEq(queryWrapper, sysMajor));
        return R.ok().put("data", list);
    }

    /**
     * 单个后端查询
     */
    @Operation(summary = "根据ID查询专业（后端）", description = "供管理后台查看详情")
    @GetMapping("/detailSysMajor_B/{id}")
    public R detailSysMajor_B(@PathVariable("id") Long id) {
        SysMajor obj = sysMajorService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 单个前端查询（公开）
     */
    @IgnoreAuth
    @Operation(summary = "根据ID查询专业（公开）", description = "无需登录即可查看")
    @GetMapping("/detailSysMajor_F/{id}")
    public R detailSysMajor_F(@PathVariable("id") Long id) {
        SysMajor obj = sysMajorService.getById(id);
        return R.ok().put("data", obj);
    }

    /**
     * 后端增加
     */
    @Operation(summary = "新增专业（后端）", description = "管理员添加记录")
    @PostMapping("/add_B")
    public R add_B(@Valid @RequestBody SysMajor sysMajor) {
        sysMajorService.save(sysMajor);
        return R.ok("添加成功").put("data", sysMajor);
    }

    /**
     * 前端增加（公开）
     */
  /*  @IgnoreAuth
    @Operation(summary = "新增专业（公开）", description = "用户自行提交，无需登录")
    @PostMapping("/add_F")
    public R add_F(@Valid @RequestBody SysMajor sysMajor) {
        sysMajorService.save(sysMajor);
        return R.ok("添加成功").put("data", sysMajor);
    }*/

    /**
     * 后端批量更新
     */
    @Operation(summary = "批量更新专业（后端）", description = "根据ID列表批量修改")
    @PutMapping("/updateSysMajor_B")
    public R updateSysMajor_B(@Valid @RequestBody List<SysMajor> sysMajors) {
        sysMajorService.updateBatchById(sysMajors);
        return R.ok();
    }

    /**
     * 前端单个更新（公开）
     */
/*    @IgnoreAuth
    @Operation(summary = "更新单个专业（公开）", description = "根据ID修改，无需登录")
    @PutMapping("/updateSysMajor_F")
    public R updateSysMajor_F(@Valid @RequestBody SysMajor sysMajor) {
        sysMajorService.updateById(sysMajor);
        return R.ok();
    }*/

    /**
     * 后端批量删除
     */
    @Operation(summary = "批量删除专业（后端）", description = "根据ID列表删除")
    @DeleteMapping("/deleteSysMajor_B")
    public R deleteSysMajor_B(@RequestBody List<Long> ids) {
        sysMajorService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 前端单个删除（公开）
     */
/*    @Operation(summary = "删除单个专业（公开）", description = "根据ID删除，无需登录")
    @DeleteMapping("/deleteSysMajor_F/{id}")
    public R deleteSysMajor_F(@PathVariable Long id) {
        sysMajorService.removeById(id);
        return R.ok();
    }*/
}