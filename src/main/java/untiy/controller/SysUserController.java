package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.entity.SysUser;
import untiy.entity.dto.SysUserDTO;
import untiy.entity.dto.ToggleStatusDTO;
import untiy.exception.Level;
import untiy.service.SysUserService;
import untiy.utils.R;
import untiy.annotation.RequiresLevel;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "用户管理", description = "用户 CRUD，数据范围与字段脱敏由后端按登录权限自动适配")
@RequestMapping("/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "分页查询用户", description = "统一查询入口，自动按权限过滤数据行并脱敏返回字段")
    @GetMapping("/listSysUser")
    public R listSysUser(@RequestParam Map<String, Object> param, SysUser sysUser) {
        IPage<SysUserDTO> page = sysUserService.pageQuery(param, sysUser);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "查询用户详情", description = "受数据范围限制，返回字段按权限脱敏")
    @GetMapping("/detailSysUser/{username}")
    public R detailSysUser(@PathVariable String username) {
        SysUserDTO dto = sysUserService.getDetail(username);
        return R.ok().put("data", dto);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "新增用户", description = "管理员及以上可调用")
    @PostMapping("/addSysUser")
    public R addSysUser(@Valid @RequestBody SysUser sysUser) {
        sysUserService.saveUser(sysUser);
        SysUserDTO dto = sysUserService.getDetail(sysUser.getUsername());
        return R.ok("添加成功").put("data", dto);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "批量启用/禁用用户", description = "目标须在数据范围内；禁用不可包含自己；更新后清除 Redis 缓存")
    @PutMapping("/toggleStatus")
    public R toggleStatus(@Valid @RequestBody ToggleStatusDTO request) {
        sysUserService.toggleStatus(request);
        return R.ok();
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "分页查询已禁用用户", description = "status=0，支持关键词模糊搜索，按管理员数据范围过滤")
    @GetMapping("/listDisabled")
    public R listDisabled(@RequestParam Map<String, Object> param,
                          @RequestParam(required = false) String keyword) {
        IPage<SysUserDTO> page = sysUserService.listDisabled(param, keyword);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "批量更新用户", description = "管理员及以上可调用；含已禁用用户则整批拒绝")
    @PutMapping("/updateSysUserBatc")
    public R updateSysUser(@Valid @RequestBody List<SysUser> sysUsers) {
        sysUserService.updateUsers(sysUsers);
        return R.ok();
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "更新用户", description = "更新自己信息")
    @PutMapping("/updateSysUser")
    public R updateSysUser(@Valid @RequestBody SysUser sysUsers) {
        sysUserService.updateUser(sysUsers);
        return R.ok();
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "根据username删除用户", description = "管理员及以上可调用")
    @DeleteMapping("/deleteSysUser/{username}")
    public R deleteSysUserByUsername(@PathVariable String username) {
        sysUserService.deleteByUsername(username);
        return R.ok("删除成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "根据username批量删除用户", description = "管理员及以上可调用，仅能删除权限范围内用户")
    @DeleteMapping("/deleteSysUser")
    public R deleteSysUser(@RequestBody List<String> names) {
        sysUserService.deleteUsers(names);
        return R.ok();
    }
}
