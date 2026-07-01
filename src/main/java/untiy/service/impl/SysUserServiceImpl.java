package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import untiy.converter.SysUserConverter;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;
import untiy.security.DataScopeHelper;
import untiy.security.FieldMaskHelper;
import untiy.security.LoginUserDetails;
import untiy.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import untiy.utils.MPUtil;

import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserConverter sysUserConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
                throw new EIException(ErrorConfig.RGEISTER_ADD_NEW_USER_CODE, "用户名已存在");
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
        return entityPage.convert(entity -> toMaskedDto(entity, viewer));
    }

    @Override
    public SysUserDTO getDetail(Long id) {
        SysUser entity = findInScope(id);
        if (entity == null) {
            return null;
        }
        return toMaskedDto(entity, DataScopeHelper.currentUser());
    }

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
        assertUsersInScope(sysUsers);
        for (SysUser user : sysUsers) {
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        updateBatchById(sysUsers);
    }

    @Transactional
    @Override
    public void deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            if (findInScope(id) == null) {
                throw new AccessDeniedException("无权删除用户：" + id);
            }
        }
        removeByIds(ids);
    }

    private SysUser findInScope(Long id) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        DataScopeHelper.applySysUserScope(wrapper);
        return baseMapper.selectOne(wrapper);
    }

    private void assertUsersInScope(List<SysUser> sysUsers) {
        if (sysUsers == null) {
            return;
        }
        for (SysUser user : sysUsers) {
            if (user.getId() == null) {
                continue;
            }
            if (findInScope(user.getId()) == null) {
                throw new AccessDeniedException("无权修改用户：" + user.getId());
            }
        }
    }

    private SysUserDTO toMaskedDto(SysUser entity, LoginUserDetails viewer) {
        SysUserDTO dto = sysUserConverter.toDto(entity);
        FieldMaskHelper.maskSysUserDto(dto, viewer);
        return dto;
    }
}
