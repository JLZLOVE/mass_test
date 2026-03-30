package untiy.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import untiy.entity.SysUser;
import untiy.service.AuthorService;
import untiy.service.LoginService;

import java.util.Collection;
import java.util.List;

@Getter

public class LoginServiceImpl implements LoginService {
    private SysUser sysUser;

    private AuthorService authorService;

    public LoginServiceImpl(SysUser sysUser, AuthorService authorService) {
        this.sysUser = sysUser;
        this.authorService = authorService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorService.getAuthoritiesByUserId(sysUser.getId());
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    @Override
    public Long getId() {
        return sysUser.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return sysUser.getStatus() == 1;
    }

    @Override
    public boolean isAccountNonLocked() {
        return sysUser.getStatus()==1;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //权限集合
    @Override
    public Collection<? extends GrantedAuthority> getAuthority() {
        Long sysUserId = sysUser.getId();
        List<GrantedAuthority> authoritiesByUserId = authorService.getAuthoritiesByUserId(sysUserId);

        return authoritiesByUserId;
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
