package com.github.netroforge.authronom_backend.service;

import com.github.netroforge.authronom_backend.service.dto.CustomOAuth2User;
import com.github.netroforge.authronom_backend.service.dto.CustomOidcUser;
import com.github.netroforge.authronom_backend.service.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * More info:
 * https://docs.spring.io/spring-authorization-server/reference/core-model-components.html#oauth2-token-customizer
 * https://docs.spring.io/spring-authorization-server/reference/guides/how-to-userinfo.html#customize-id-token
 */
@Slf4j
public final class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            Authentication principalParent = context.getPrincipal();
            Object principal = principalParent.getPrincipal();

            if (principal instanceof CustomOAuth2User customOAuth2User) {
                context.getClaims().claims(existingClaims -> {
                    existingClaims.put(IdTokenClaimNames.SUB, customOAuth2User.getUid());
                });
            } else if (principal instanceof CustomOidcUser customOidcUser) {
                context.getClaims().claims(existingClaims -> {
                    existingClaims.put(IdTokenClaimNames.SUB, customOidcUser.getUid());
                });
            } else if (principal instanceof CustomUserDetails customUserDetails) {
                context.getClaims().claims(existingClaims -> {
                    existingClaims.put(IdTokenClaimNames.SUB, customUserDetails.getUid());
                });
            }

            log.debug("Federated identity id token custom principal: {}", principal);
        }
    }
}
