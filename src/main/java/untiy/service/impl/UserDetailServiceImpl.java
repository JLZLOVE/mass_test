package untiy.service.impl;

import lombok.extern.slf4j.Slf4j;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private DataSource dataSource;

    /*@Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;
    */
    @Autowired
    private AuthorService authorService;
//用户登录,并获取对应权限各项信息
    @Transactional
    public LoginServiceImpl loadUserById(String id, String password) {
        log.info("🔥 进入了 loadUserById，传入参数：{}, {}", id, password);
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
     /*   if (!passwordEncoder.matches(password, sysUser.getPassword())) {
            throw new  EIException(ErrorConfig.RGEISTER_PASSWORD_CODE,ErrorConfig.RGEISTER_PASSWORD_MSG);
        }
*/

        return new LoginServiceImpl(sysUser, authorService);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里 username 是登录时传入的学号/工号，
        log.info("1 进入了 loadUserByUsername，传入参数：{}", username);
        SysUser sysUser = sysUserMapper.selectByUsername(username);
//        阻塞线程池加量发现需要配置连接池
        log.info("2 MyBatis查询完成，查到用户：{}", sysUser);
        //        用户为空抛出异常
        if (Objects.isNull(sysUser)) {
            log.error("3 用户为空，抛出异常");
            throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE, ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }

        log.info("4开始组装LoginServiceImpl对象");

        LoginServiceImpl loginService = new LoginServiceImpl(sysUser, authorService);
        log.info("5 组装完成，返回UserDetails");
        return loginService;

    }
/*@Override
public UserDetails loadUserByUsername(String username) {
    log.info("1 进入了 loadUserByUsername，传入参数：{}", username);
    // 临时注释MyBatis Mapper调用
    // SysUser sysUser = sysUserMapper.selectByUsername(username);

    // 手动JDBC硬编码查询测试
    SysUser sysUser = null;
    try(Connection conn = dataSource.getConnection();
        PreparedStatement pst = conn.prepareStatement("select * from sys_user where username = ?")){
        pst.setString(1,username);
        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            sysUser = new SysUser();
            sysUser.setId(rs.getLong("id"));
            sysUser.setUsername(rs.getString("username"));
            sysUser.setPassword(rs.getString("password"));
        }
        rs.close();
    }catch (Exception e){
        e.printStackTrace();
    }
    log.info("2 MyBatis查询完成，查到用户：{}", sysUser);
    if (Objects.isNull(sysUser)) {
        log.error("3 用户为空，抛出异常");
        throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE, ErrorConfig.RGEISTER_STATUS_CODE_MSG);
    }
    log.info("4开始组装LoginServiceImpl对象");
    LoginServiceImpl loginService = new LoginServiceImpl(sysUser, authorService);
    log.info("5 组装完成，返回UserDetails");
    return loginService;
}*/
}
