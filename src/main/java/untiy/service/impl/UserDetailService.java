package untiy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.config.ErrorConfig;
import untiy.entity.EIException;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;

import java.util.Objects;

@Service
public class UserDetailService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
@Transactional
    public LoginServiceImpl loadUserById(String id, String password) {
        if (id.isEmpty()|| password.isEmpty()) {
            throw new EIException(ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE,ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_MSG);
        }



        SysUser sysUser = sysUserMapper.selectByStatusId(id);
        if (Objects.isNull(sysUser)) {
            throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE,ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }
        if (!passwordEncoder.matches(password, sysUser.getPassword())) {
            throw new  EIException(ErrorConfig.RGEISTER_PASSWORD_CODE,ErrorConfig.RGEISTER_PASSWORD_MSG);
        }

        return new LoginServiceImpl(sysUser);
    }

}
