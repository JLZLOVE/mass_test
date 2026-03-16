package untiy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.service.impl.LoginServiceImpl;
import untiy.service.impl.UserDetailServiceImpl;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    UserDetailServiceImpl userDetailService;

    @PostMapping("/allocation")
    public LoginServiceImpl login(@RequestParam String name, @RequestParam String password, HttpServletRequest httpServletRequest) {

        LoginServiceImpl loginService = userDetailService.loadUserById(name, password);
        return loginService;
    }
}
