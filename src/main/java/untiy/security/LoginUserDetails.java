package untiy.security;

import lombok.Getter;
import untiy.entity.SysUser;
import untiy.service.AuthorService;
import untiy.service.impl.LoginServiceImpl;

import java.io.Serializable;

/**
 * 登录用户详情：组合 {@link SysUser}、有效权限等级、社团/部门范围。
 * <p>
 * 继承 {@link LoginServiceImpl} 以兼容 {@link untiy.controller.LoginController} 现有强转逻辑。
 */
@Getter
public class LoginUserDetails extends LoginServiceImpl {

    /**
     * 有效等级（0=超管全部，1=管理员看学生，2=社长看社团，3=部长看部门，4=仅自己）
     */
    private final int effectiveLevel;

    /** 主社团 ID（scope_type=2） */
    private final Long primaryClubId;

    /** 主部门 ID（scope_type=3） */
    private final Long primaryDepartmentId;

    public LoginUserDetails(SysUser sysUser,
                            AuthorService authorService,
                            int effectiveLevel,
                            Long primaryClubId,
                            Long primaryDepartmentId) {
        super(sysUser, authorService);
        this.effectiveLevel = effectiveLevel;
        this.primaryClubId = primaryClubId;
        this.primaryDepartmentId = primaryDepartmentId;
    }

    /** 与切面、BaseQuery 注入字段对齐 */
    public Long getUserId() {
        return getId();
    }

    public CacheSnapshot toCacheSnapshot() {
        SysUser u = getSysUser();
        return new CacheSnapshot(
                u.getId(), u.getUsername(), u.getPassword(), u.getStatus(),
                effectiveLevel, primaryClubId, primaryDepartmentId);
    }

    public static LoginUserDetails fromCacheSnapshot(CacheSnapshot snapshot, AuthorService authorService) {
        SysUser user = new SysUser();
        user.setId(snapshot.userId);
        user.setUsername(snapshot.username);
        user.setPassword(snapshot.password);
        user.setStatus(snapshot.status);
        return new LoginUserDetails(
                user, authorService,
                snapshot.effectiveLevel, snapshot.primaryClubId, snapshot.primaryDepartmentId);
    }

    /** Redis 可序列化快照（不含 AuthorService） */
    public static class CacheSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Long userId;
        private final String username;
        private final String password;
        private final Integer status;
        private final int effectiveLevel;
        private final Long primaryClubId;
        private final Long primaryDepartmentId;

        public CacheSnapshot(Long userId, String username, String password, Integer status,
                             int effectiveLevel, Long primaryClubId, Long primaryDepartmentId) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.status = status;
            this.effectiveLevel = effectiveLevel;
            this.primaryClubId = primaryClubId;
            this.primaryDepartmentId = primaryDepartmentId;
        }
    }
}
