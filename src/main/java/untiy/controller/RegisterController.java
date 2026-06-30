package untiy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import untiy.annotion.IgnoreAuth;
import untiy.entity.RegisterDTO;
import untiy.service.SysUserService;
import untiy.utils.R;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    SysUserService sysUserService;

    @IgnoreAuth
    @PostMapping("/single")
    public R register(@RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return R.ok("注册成功");
    }
}
