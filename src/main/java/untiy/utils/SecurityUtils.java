package untiy.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.security.LoginUserDetails;

@Component
public class SecurityUtils {
    public static LoginUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUserDetails)) {
            throw new EIException(ErrorConfig.LOGIN_INVALID_MSG);
        }
        return (LoginUserDetails) auth.getPrincipal();
    }
    public static int getCurrentLevel() {
        return getCurrentUser().getEffectiveLevel();
    }
}