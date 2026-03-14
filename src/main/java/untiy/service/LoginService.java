package untiy.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;

import java.util.Collection;

public interface LoginService {
    String getPassword();

   Long getId();

    Boolean isAccountNonExpired();
    Boolean isAcoountNotLock();
    Collection<? extends GrantedAuthority > getAuthority();
    boolean isCredentialNonExpired();
    boolean isEnabled();
}
