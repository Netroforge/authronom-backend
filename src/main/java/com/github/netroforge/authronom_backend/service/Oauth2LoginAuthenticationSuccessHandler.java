package com.github.netroforge.authronom_backend.service;

import com.github.netroforge.authronom_backend.properties.SecurityProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class Oauth2LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final SavedRequestAwareAuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    private final UserRepositoryOAuth2UserHandler userRepositoryOAuth2UserHandler;

    private final HttpSessionRequestCache httpSessionRequestCache = new HttpSessionRequestCache();
    private final SecurityProperties securityProperties;

    public Oauth2LoginAuthenticationSuccessHandler(
            UserRepositoryOAuth2UserHandler userRepositoryOAuth2UserHandler,
            SecurityProperties securityProperties
    ) {
        this.userRepositoryOAuth2UserHandler = userRepositoryOAuth2UserHandler;
        this.securityProperties = securityProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            String authorizedClientRegistrationId =
                    ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            if (authentication.getPrincipal() instanceof OidcUser) {
                userRepositoryOAuth2UserHandler.accept(
                        (OidcUser) authentication.getPrincipal(),
                        authorizedClientRegistrationId
                );
            }
        }

        SavedRequest savedRequest = httpSessionRequestCache.getRequest(request, response);
        if (savedRequest == null) {
            httpSessionRequestCache.removeRequest(request, response);
            clearAuthenticationAttributes(request);
        }

        delegate.setDefaultTargetUrl(securityProperties.getOauth2LoginSuccessUrl());
        delegate.setAlwaysUseDefaultTargetUrl(true);
        delegate.onAuthenticationSuccess(request, response, authentication);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
