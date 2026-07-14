package untiy.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import untiy.entity.SysUser;
import untiy.service.AuthorService;
import untiy.service.impl.LoginServiceImpl;

import java.io.Serializable;

/**
 * 登录用户详情：组合 {@link SysUser}、有效权限等级、社团/部门范围。
 */
@Getter
public class LoginUserDetails extends LoginServiceImpl {

    private final int effectiveLevel;

    private final Long primaryClubId;

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
        user.setId(snapshot.getUserId());
        user.setUsername(snapshot.getUsername());
        user.setPassword(snapshot.getPassword());
        user.setStatus(snapshot.getStatus());
        return new LoginUserDetails(
                user, authorService,
                snapshot.getEffectiveLevel(), snapshot.getPrimaryClubId(), snapshot.getPrimaryDepartmentId());
    }

    /** Redis 可序列化快照（不含 AuthorService） */
    @Getter
    public static class CacheSnapshot implements Serializable {

        private static final long serialVersionUID = 2L;

        @JsonProperty("userId")
        private final Long userId;

        @JsonProperty("username")
        private final String username;

        @JsonProperty("password")
        private final String password;

        @JsonProperty("status")
        private final Integer status;

        @JsonProperty("effectiveLevel")
        private final int effectiveLevel;

        @JsonProperty("primaryClubId")
        private final Long primaryClubId;

        @JsonProperty("primaryDepartmentId")
        private final Long primaryDepartmentId;

        @JsonCreator
        public CacheSnapshot(
                @JsonProperty("userId") Long userId,
                @JsonProperty("username") String username,
                @JsonProperty("password") String password,
                @JsonProperty("status") Integer status,
                @JsonProperty("effectiveLevel") int effectiveLevel,
                @JsonProperty("primaryClubId") Long primaryClubId,
                @JsonProperty("primaryDepartmentId") Long primaryDepartmentId) {
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
