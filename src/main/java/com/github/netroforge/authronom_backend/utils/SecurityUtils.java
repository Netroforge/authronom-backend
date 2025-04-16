package com.github.netroforge.authronom_backend.utils;

import com.github.netroforge.authronom_backend.service.dto.CustomOAuth2User;
import com.github.netroforge.authronom_backend.service.dto.CustomOidcUser;
import com.github.netroforge.authronom_backend.service.dto.CustomUserDetails;
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

        // Handle different authentication types
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUid();
        } else if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUid();
        } else if (principal instanceof CustomOidcUser oidcUser) {
            return oidcUser.getUid();
        } else if (principal instanceof String username) {
            // Handle mock user from @WithMockUser
            return username;
        }

        log.info("Principal class = {} is not supported", principal.getClass().getSimpleName());
        throw new IllegalStateException("Must be authenticated");
    }
}
