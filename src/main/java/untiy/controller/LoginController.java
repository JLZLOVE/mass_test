package untiy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.entity.SysUser;
import untiy.service.LoginService;
import untiy.service.impl.LoginServiceImpl;
import untiy.service.impl.UserDetailService;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    UserDetailService userDetailService;

    @PostMapping("/allocation")
    public LoginServiceImpl login(@RequestParam String name, @RequestParam String password) {

        LoginServiceImpl loginService = userDetailService.loadUserById(name, password);
        return loginService;
    }
}
