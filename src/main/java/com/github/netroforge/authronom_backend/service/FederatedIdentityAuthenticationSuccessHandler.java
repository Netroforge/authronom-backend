package com.github.netroforge.authronom_backend.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class FederatedIdentityAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

	private final UserRepositoryOAuth2UserHandler userRepositoryOAuth2UserHandler;

    public FederatedIdentityAuthenticationSuccessHandler(UserRepositoryOAuth2UserHandler userRepositoryOAuth2UserHandler) {
        this.userRepositoryOAuth2UserHandler = userRepositoryOAuth2UserHandler;
    }

    @Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException, ServletException {
		if (authentication instanceof OAuth2AuthenticationToken) {
			if (authentication.getPrincipal() instanceof OidcUser) {
				userRepositoryOAuth2UserHandler.accept((OidcUser) authentication.getPrincipal());
			} else if (authentication.getPrincipal() instanceof OAuth2User) {
				userRepositoryOAuth2UserHandler.accept((OAuth2User) authentication.getPrincipal());
			}
		}

		this.delegate.onAuthenticationSuccess(request, response, authentication);
	}
}
