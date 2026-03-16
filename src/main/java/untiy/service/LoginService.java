package untiy.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface LoginService extends UserDetails {
    String getPassword();

    Long getId();

    boolean isAccountNonExpired();

    Collection<? extends GrantedAuthority> getAuthority();

    boolean isCredentialNonExpired();

    boolean isEnabled();
}
