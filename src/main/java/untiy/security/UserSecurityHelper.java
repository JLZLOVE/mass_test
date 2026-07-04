package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.access.AccessDeniedException;
import untiy.converter.SysUserConverter;
import untiy.entity.SysUser;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.ErrorConfig;
import untiy.exception.EIException;
import untiy.mapper.SysUserMapper;

import java.util.List;

/**
 * 用户模块专用安全工具：数据范围校验、状态校验与 DTO 脱敏。
 */
public final class UserSecurityHelper {

    private UserSecurityHelper() {
    }

    public static void assertUsersInScope(SysUserMapper mapper, List<SysUser> sysUsers) {
        if (sysUsers == null) {
            return;
        }
        for (SysUser user : sysUsers) {
            if (user.getId() == null) {
                continue;
            }
            if (findInScope(mapper, user.getUsername()) == null) {
                throw new AccessDeniedException(ErrorConfig.NO_PERM_DELETE_USER_MSG + user.getId());
            }
        }
    }

    public static SysUserDTO toMaskedDto(SysUserConverter converter, SysUser entity, LoginUserDetails viewer) {
        SysUserDTO dto = converter.toDto(entity);
        FieldMaskHelper.maskSysUserDto(dto, viewer);
        return dto;
    }

    public static SysUser findInScope(SysUserMapper mapper, String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        DataScopeHelper.applySysUserScope(wrapper);
        return mapper.selectOne(wrapper);
    }

    public static SysUser findInScopeByUserId(SysUserMapper mapper, Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = mapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return findInScope(mapper, user.getUsername());
    }

    /** 在数据范围内且未被禁用 */
    public static SysUser findActiveInScope(SysUserMapper mapper, Long userId) {
        SysUser user = findInScopeByUserId(mapper, userId);
        assertUserEnabled(user);
        return user;
    }

    public static void assertUserEnabled(SysUser user) {
        if (user == null) {
            throw new EIException(ErrorConfig.RGEISTER_STATUS_CODE, ErrorConfig.RGEISTER_STATUS_CODE_MSG);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new EIException(ErrorConfig.USER_DISABLED_CODE, ErrorConfig.USER_DISABLED_MSG);
        }
    }

    public static void assertBatchNoDisabled(SysUserMapper mapper, List<SysUser> sysUsers) {
        if (sysUsers == null) {
            return;
        }
        for (SysUser input : sysUsers) {
            SysUser dbUser = input.getUsername() != null
                    ? mapper.selectByUsername(input.getUsername())
                    : (input.getId() != null ? mapper.selectById(input.getId()) : null);
            if (dbUser != null && (dbUser.getStatus() == null || dbUser.getStatus() != 1)) {
                throw new EIException(ErrorConfig.BATCH_CONTAINS_DISABLED_CODE, ErrorConfig.BATCH_CONTAINS_DISABLED_MSG);
            }
        }
    }
}
