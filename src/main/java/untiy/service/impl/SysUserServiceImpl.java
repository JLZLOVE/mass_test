package untiy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;
import untiy.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * <p>
 * 用户基础表 服务实现类
 * </p>
 *
 * @author 玖
 * @since 2026-02-19
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SysUserMapper sysUserMapper;
/*@Transactional
//注册
@Override
    public void register(RegisterDTO registerDTO) {
//        学号
        String realName = registerDTO.getRealName();
//        加密密码
        String password = registerDTO.getPassword();
        String encode = passwordEncoder.encode(password);
//        性别
        Integer gender = registerDTO.getGender();
//        存入
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("password", encode);
        stringStringHashMap.put("name", realName);
        stringStringHashMap.put("gender",gender);
//        设置id


        boolean right = sysUserMapper.addNewUser(stringStringHashMap);
        if (right) {
            throw new EIException(ErrorConfig.RGEISTER_ADD_NEW_USER_CODE,ErrorConfig.RGEISTER_ADD_NEW_USER_MSG);
        }
//        注册成功

    }*/
}
