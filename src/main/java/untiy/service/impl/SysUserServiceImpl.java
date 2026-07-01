package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import untiy.converter.SysUserConverter;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.entity.RegisterDTO;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;
import untiy.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import untiy.utils.MPUtil;
import untiy.utils.R;

import java.util.List;
import java.util.Map;

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
    private SysUserConverter sysUserConverter;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SysUserMapper sysUserMapper;

    @Transactional
    @Override
//    注册逻辑
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
    public IPage<SysUserDTO> pageQueryDTO(Map<String, Object> param, SysUser sysUser) {
        // 1. 分页逻辑
        Page<SysUser> page = MPUtil.getPage(param);

        // 2. 条件构建
        QueryWrapper<SysUser> wrapper = MPUtil.sort(
                MPUtil.between(
                        MPUtil.likeOrEq(new QueryWrapper<>(), sysUser),
                        param
                ),
                param
        );

        // 3. 调 baseMapper.selectPage()
        //
        IPage<SysUser> entityPage = baseMapper.selectPage(page, wrapper);

        // 4. 实体转 DTO（用你的 MapStruct）

        return entityPage.convert(sysUserConverter::toDto);
    }

    @Override
    public IPage<SysUser> pageQueryFront(Map<String, Object> param, SysUser sysUser) {

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysUser);
        MPUtil.between(queryWrapper, param);
        MPUtil.sort(queryWrapper, param);
        Page<SysUser> page = MPUtil.getPage(param);
        // 用 baseMapper.selectPage，不要调 this.page()
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<SysUser> queryByCondition(SysUser sysUser) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        MPUtil.likeOrEq(queryWrapper, sysUser);
        return baseMapper.selectList(queryWrapper);
    }

}
