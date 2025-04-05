package com.github.netroforge.authronom_backend.utils;

import com.github.netroforge.authronom_backend.service.dto.AuthorizedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public final class SecurityUtils {

    public static AuthorizedUser getAuthorizedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Must be authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthorizedUser authorizedUser) {
            return authorizedUser;
        }

        log.info("Principal class = {} is not supported", principal.getClass().getSimpleName());
        throw new IllegalStateException("Must be authenticated");
    }

}
