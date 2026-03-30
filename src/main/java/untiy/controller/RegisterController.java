package untiy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.entity.RegisterDTO;
import untiy.service.SysUserService;
import untiy.utils.R;
//废弃,不再提供注册接口
@RestController
@RequestMapping("register")
public class RegisterController {

    @Autowired
    SysUserService sysUserService;

    /*    @PostMapping("single")
        public R register_test(@RequestParam String name, @RequestParam String password) {
            if (name == null || password == null || name.isEmpty() || password.isEmpty()) {
                throw new EIException("姓名或密码不能为空");
            }

            sysUserService.register(name, password);
            return R.ok("注册成功");
        }*/
/*    @PostMapping("single")
    public R register(@RequestBody RegisterDTO registerDTO) {
        String realName = registerDTO.getRealName();
        String password = registerDTO.getPassword();
        if (realName == null || password == null || realName.isEmpty() || realName.isEmpty()) {
            throw new EIException(ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE,ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_MSG);
        }
        sysUserService.register(registerDTO);
        return R.ok("注册成功");
    }*/
}
