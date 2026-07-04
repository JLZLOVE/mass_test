package untiy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import untiy.converter.SysUserConverter;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.exception.UserPermissionCode;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;
import untiy.security.DataScopeHelper;
import untiy.security.LoginUserDetails;
import untiy.security.UserSecurityHelper;
import untiy.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import untiy.utils.MPUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserConverter sysUserConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //注册逻辑
    @Transactional
    @Override
    public void register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String realName = registerDTO.getRealName();
        String password = registerDTO.getPassword();
        if (realName == null || realName.isEmpty() || password == null || password.isEmpty()) {
            throw new EIException(ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_CODE, ErrorConfig.RGEISTER_PASSWORD_OR_NAMEC_MSG);
        }
        if (username != null && !username.isEmpty()) {
            SysUser existing = lambdaQuery().eq(SysUser::getUsername, username).one();
            if (existing != null) {
                throw new EIException(ErrorConfig.RGEISTER_ADD_NEW_USER_CODE, ErrorConfig.Author_ID_MSG);
            }
        } else {
            username = realName;
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setRealName(realName);
        user.setPassword(passwordEncoder.encode(password));
        user.setGender(registerDTO.getGender() != null ? registerDTO.getGender() : 0);
        user.setUserType(registerDTO.getUserType() != null ? registerDTO.getUserType() : 1);
        user.setStatus(1);
        save(user);
    }

    //分页查询
    @Override
    public IPage<SysUserDTO> pageQuery(Map<String, Object> param, SysUser sysUser) {
        Page<SysUser> page = MPUtil.getPage(param);
        QueryWrapper<SysUser> wrapper = MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysUser),
                        param
                ),
                param
        );
        DataScopeHelper.applySysUserScope(wrapper);

        IPage<SysUser> entityPage = baseMapper.selectPage(page, wrapper);
        LoginUserDetails viewer = DataScopeHelper.currentUser();
        return entityPage.convert(entity -> UserSecurityHelper.toMaskedDto(sysUserConverter, entity, viewer));
    }

    //查询细节
    @Override
    public SysUserDTO getDetail(String username) {
        SysUser entity = sysUserMapper.selectByUsername(username);
        if (entity != null) {
           /* log.info("entity.getId() = {}", entity.getId());
            log.info("entity.getUsername() = '{}'", entity.getUsername());
            log.info("entity.getRealName() = {}", entity.getRealName());
            log.info("entity.getPhone() = {}", entity.getPhone());
*/
        } else {
            throw new EIException(ErrorConfig.USERNAME_BLANK_CODE,ErrorConfig.USERNAME_BLANK_MSG);
        }

        if ( entity.getUsername() == null) {
            throw new EIException(ErrorConfig.USERNAME_BLANK_CODE,ErrorConfig.USERNAME_BLANK_MSG);
        }

        return UserSecurityHelper.toMaskedDto(sysUserConverter, entity, DataScopeHelper.currentUser());
    }

    // 添加用户
    @Transactional
    @Override
    public void saveUser(SysUser sysUser) {
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }
        save(sysUser);
    }

    @Transactional
    @Override
    public void updateUsers(List<SysUser> sysUsers) {
        UserSecurityHelper.assertUsersInScope(sysUserMapper, sysUsers);
        for (SysUser user : sysUsers) {
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        updateBatchById(sysUsers);
    }

    @Transactional
    @Override
    public void deleteUsers(List<String> names) {
        if (names == null || names.isEmpty()) {
            return;
        }
        for (String name : names) {
            if (UserSecurityHelper.findInScope(sysUserMapper, name) == null) {
                throw new AccessDeniedException(ErrorConfig.NO_PERM_DELETE_USER_MSG + names);
            }
        }
        baseMapper.deleteByUsernames(names);
    }

    @Transactional
    @Override
    public void deleteByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        if (UserSecurityHelper.findInScope(sysUserMapper, username) == null) {
            throw new AccessDeniedException(ErrorConfig.NO_PERM_DELETE_USER_MSG + username);
        }
        baseMapper.deleteByUsername(username);
    }

    //更新自己
    @Transactional
    @Override
    public void updateUser(SysUser sysUser) {
        // 1. 获取当前登录用户
        LoginUserDetails currentUser = DataScopeHelper.currentUser();
        if (currentUser == null) {
            throw new EIException(ErrorConfig.NOT_LOGGED_IN_CODE, ErrorConfig.NOT_LOGGED_IN_MSG);
        }

        // 2. 参数校验：必须有 username 才能定位目标
        if (sysUser == null || sysUser.getUsername() == null || sysUser.getUsername().trim().isEmpty()) {
            throw new EIException(UserPermissionCode.TARGET_NOT_FOUND_CODE, UserPermissionCode.TARGET_NOT_FOUND_MSG);
        }

        // 3. 安全防护：只能改自己
        if (!currentUser.getUsername().equals(sysUser.getUsername())) {
            throw new EIException(UserPermissionCode.PERMISSION_DENIED_CODE, UserPermissionCode.PERMISSION_DENIED_MSG);
        }

        // 4. 从数据库查出最新的完整实体（防止缓存/脏数据）
        SysUser dbUser = lambdaQuery().eq(SysUser::getUsername, sysUser.getUsername()).one();
        if (dbUser == null) {
            throw new EIException(UserPermissionCode.TARGET_NOT_FOUND_CODE, UserPermissionCode.TARGET_NOT_FOUND_MSG);
        }

        // 5. 仅提取白名单字段，其他全部当空气
        lambdaUpdate()
                .eq(SysUser::getUsername, dbUser.getUsername())
                .set(SysUser::getRealName, sysUser.getRealName())
                .set(SysUser::getGender, sysUser.getGender())
                .set(SysUser::getPhone, sysUser.getPhone())
                .set(SysUser::getEmail, sysUser.getEmail())
                .set(SysUser::getAvatar, sysUser.getAvatar())
                .update();
    }
}
