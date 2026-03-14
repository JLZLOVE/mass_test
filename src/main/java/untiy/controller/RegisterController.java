package untiy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import untiy.config.ErrorConfig;
import untiy.entity.EIException;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.service.SysUserService;
import untiy.utils.R;

import javax.websocket.server.PathParam;
import java.util.HashMap;

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
    @PostMapping("single")
    public R register(@RequestBody RegisterDTO registerDTO) {
        String realName = registerDTO.getRealName();
        String password = registerDTO.getPassword();
        if (realName == null || password == null || realName.isEmpty() || realName.isEmpty()) {
            throw new EIException(ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE,ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_MSG);
        }
        sysUserService.register(registerDTO);
        return R.ok("注册成功");
    }
}
