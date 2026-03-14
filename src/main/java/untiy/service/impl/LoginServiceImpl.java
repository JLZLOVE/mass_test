package untiy.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import untiy.entity.SysUser;
import untiy.service.LoginService;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginServiceImpl implements LoginService {
    private SysUser sysUser;

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public Long getId() {
        return sysUser.getId();
    }

    //账户是否过期
    @Override
    public Boolean isAccountNonExpired() {
        return null;
    }

    //账户锁定
    @Override
    public Boolean isAcoountNotLock() {
        return sysUser.getStatus() == 1;
    }

    //权限集合
    @Override
    public Collection<? extends GrantedAuthority> getAuthority() {
        return null;
    }

    //凭证是否过期
    @Override
    public boolean isCredentialNonExpired() {
        return false;
    }

    //账户是否启用
    @Override
    public boolean isEnabled() {
        return sysUser.getStatus() == 1;
    }
}
