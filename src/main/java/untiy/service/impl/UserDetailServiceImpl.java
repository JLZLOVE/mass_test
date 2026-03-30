package untiy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;
import untiy.service.AuthorService;

import java.util.Objects;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserMapper sysUserMapper;
/*@Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;
    */
    @Autowired
    private AuthorService authorService;
//用户登录,并获取对应权限各项信息
    @Transactional
    public LoginServiceImpl loadUserById(String id, String password) {
//    先判断,值如何再进行操作
        if (Objects.isNull(password) || Objects.isNull(id) || id.isEmpty() || password.isEmpty()) {
            throw new EIException(ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE, ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_MSG);
        }


//根据编号查询用户
        SysUser sysUser = sysUserMapper.selectByStatusId(id);
//        用户为空抛出异常
        if (Objects.isNull(sysUser)) {
            throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE, ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }
//        检查密码是否匹配,框架自动匹配
    /*    if (!passwordEncoder.matches(password, sysUser.getPassword())) {
            throw new  EIException(ErrorConfig.RGEISTER_PASSWORD_CODE,ErrorConfig.RGEISTER_PASSWORD_MSG);
        }*/


        return new LoginServiceImpl(sysUser, authorService);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里 username 是登录时传入的学号/工号，
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException(ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }
        return new LoginServiceImpl(sysUser, authorService);
    }
}
