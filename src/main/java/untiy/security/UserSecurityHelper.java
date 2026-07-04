package untiy.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.access.AccessDeniedException;
import untiy.converter.SysUserConverter;
import untiy.entity.SysUser;
import untiy.entity.dto.SysUserDTO;
import untiy.exception.ErrorConfig;
import untiy.mapper.SysUserMapper;

import java.util.List;

/**
 * 用户模块专用安全工具：数据范围校验与 DTO 脱敏，仅限 SysUser 相关业务复用。
 */
public final class UserSecurityHelper {

    private UserSecurityHelper() {
    }

    /**
     * 批量校验待操作用户是否落在当前登录者的数据范围内。
     * <p>
     * 逐个按 username 查询并叠加 {@link DataScopeHelper#applySysUserScope} 条件，
     * 查不到则视为越权，抛出 {@link AccessDeniedException}。
     *
     * @param mapper   用户 Mapper，用于范围查询
     * @param sysUsers 待校验用户列表，{@code id} 为 null 的条目跳过
     */
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

    /**
     * 将用户实体转为 DTO，并按查看者权限等级脱敏敏感字段。
     *
     * @param converter Entity → DTO 转换器
     * @param entity    数据库用户实体
     * @param viewer    当前查看者，可为 null（此时清空敏感字段）
     * @return 脱敏后的 {@link SysUserDTO}
     */
    public static SysUserDTO toMaskedDto(SysUserConverter converter, SysUser entity, LoginUserDetails viewer) {
        SysUserDTO dto = converter.toDto(entity);
        FieldMaskHelper.maskSysUserDto(dto, viewer);
        return dto;
    }

    /**
     * 按 username 查询用户，并叠加当前登录者的数据范围条件。
     *
     * @return 范围内的用户实体；不在范围内或不存在时返回 null
     */
    public static SysUser findInScope(SysUserMapper mapper, String username) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        DataScopeHelper.applySysUserScope(wrapper);
        return mapper.selectOne(wrapper);
    }
}
