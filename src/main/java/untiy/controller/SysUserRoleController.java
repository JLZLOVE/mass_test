package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import untiy.annotation.RequiresLevel;
import untiy.entity.dto.AssignRoleDTO;
import untiy.entity.vo.SysUserRoleVO;
import untiy.exception.Level;
import untiy.service.SysUserRoleService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户-角色关联：仅保留 assign / revoke / list / my-roles 四个接口。
 */
@RestController
@Tag(name = "用户角色关联", description = "角色分配、撤销与查询")
@RequestMapping("/sys-user-role")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "分配角色", description = "目标用户须在范围内且未禁用；校验 scope 与防重复")
    @PostMapping("/assign")
    public R assign(@Valid @RequestBody AssignRoleDTO dto) {
        sysUserRoleService.assign(dto);
        return R.ok("分配成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "撤销角色", description = "不能撤销高于自身等级的角色，不能撤销自己持有的角色")
    @DeleteMapping("/revoke/{id}")
    public R revoke(@PathVariable Long id) {
        sysUserRoleService.revoke(id);
        return R.ok("撤销成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "分页查询用户角色列表", description = "联查用户姓名、角色名称，按管理员数据范围过滤")
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> param,
                  @RequestParam(required = false) String keyword) {
        IPage<SysUserRoleVO> page = sysUserRoleService.pageQuery(param, keyword);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "查询自己的角色", description = "仅返回当前登录用户的角色列表")
    @GetMapping("/my-roles")
    public R myRoles() {
        List<SysUserRoleVO> list = sysUserRoleService.listMyRoles();
        return R.ok().put("data", list);
    }
}
