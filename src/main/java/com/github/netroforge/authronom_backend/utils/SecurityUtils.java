package com.github.netroforge.authronom_backend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public final class SecurityUtils {
    public static String getAuthorizedUserUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Must be authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        log.info("Principal class = {} is not supported", principal.getClass().getSimpleName());
        throw new IllegalStateException("Must be authenticated");
    }
}
