package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import untiy.annotation.IgnoreAuth;
import untiy.entity.RegisterDTO;
import untiy.service.SysUserService;
import untiy.utils.R;

@RestController
@Tag(name = "用户注册", description = "用户注册接口，提供公开注册功能")
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    SysUserService sysUserService;

    @IgnoreAuth
    @Operation(summary = "用户注册", description = "使用注册信息（学号/工号、密码、姓名等）创建新用户，无需登录")
    @PostMapping("/single")
    public R register(@RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return R.ok("注册成功");
    }
}