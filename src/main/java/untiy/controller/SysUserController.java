package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysUser;
import untiy.entity.dto.SysUserDTO;
import untiy.service.SysUserService;
import untiy.utils.R;
import untiy.annotation.RequiresLevel;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户管理：每个业务功能仅保留单一接口，权限差异由 Service 层处理。
 */
@RestController
@Tag(name = "用户管理", description = "用户 CRUD，数据范围与字段脱敏由后端按登录权限自动适配")
@RequestMapping("/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @RequiresLevel(minLevel = 4)
    @Operation(summary = "分页查询用户", description = "统一查询入口，自动按权限过滤数据行并脱敏返回字段")
    @GetMapping("/listSysUser")
    public R listSysUser(@RequestParam Map<String, Object> param, SysUser sysUser) {
        IPage<SysUserDTO> page = sysUserService.pageQuery(param, sysUser);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = 4)
    @Operation(summary = "查询用户详情", description = "受数据范围限制，返回字段按权限脱敏")
    @GetMapping("/detailSysUser/{id}")
    public R detailSysUser(@PathVariable("id") Long id) {
        SysUserDTO dto = sysUserService.getDetail(id);
        return R.ok().put("data", dto);
    }

    @RequiresLevel(minLevel = 1)
    @Operation(summary = "新增用户", description = "管理员及以上可调用")
    @PostMapping("/addSysUser")
    public R addSysUser(@Valid @RequestBody SysUser sysUser) {
        sysUserService.saveUser(sysUser);
        SysUserDTO dto = sysUserService.getDetail(sysUser.getId());
        return R.ok("添加成功").put("data", dto);
    }

    @RequiresLevel(minLevel = 1)
    @Operation(summary = "批量更新用户", description = "管理员及以上可调用，仅能修改权限范围内用户")
    @PutMapping("/updateSysUser")
    public R updateSysUser(@Valid @RequestBody List<SysUser> sysUsers) {
        sysUserService.updateUsers(sysUsers);
        return R.ok();
    }

    @RequiresLevel(minLevel = 1)
    @Operation(summary = "批量删除用户", description = "管理员及以上可调用，仅能删除权限范围内用户")
    @DeleteMapping("/deleteSysUser")
    public R deleteSysUser(@RequestBody List<Long> ids) {
        sysUserService.deleteUsers(ids);
        return R.ok();
    }
}
