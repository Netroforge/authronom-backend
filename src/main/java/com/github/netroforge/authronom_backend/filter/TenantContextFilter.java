package com.github.netroforge.authronom_backend.filter;

import com.github.netroforge.authronom_backend.db.repository.UserRepository;
import com.github.netroforge.authronom_backend.db.repository.entity.User;
import com.github.netroforge.authronom_backend.utils.SecurityUtils;
import com.github.netroforge.authronom_backend.utils.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Set default tenant for public endpoints
            TenantContextHolder.setTenantId("default");

            // Try to get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    String userUid = SecurityUtils.getAuthorizedUserUid();
                    if (userUid != null) {
                        User user = userRepository.findByUid(userUid);
                        if (user != null && user.getTenantUid() != null) {
                            TenantContextHolder.setTenantId(user.getTenantUid());
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not determine tenant for user: {}", e.getMessage());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Clear the tenant context after the request is processed
            TenantContextHolder.clear();
        }
    }
}
