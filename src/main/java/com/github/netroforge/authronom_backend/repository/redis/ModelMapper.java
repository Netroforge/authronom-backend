package com.github.netroforge.authronom_backend.repository.redis;

import com.github.netroforge.authronom_backend.repository.redis.entity.OAuth2RegisteredClient;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class ModelMapper {
    public static OAuth2RegisteredClient convertOAuth2RegisteredClient(RegisteredClient registeredClient) {
        OAuth2RegisteredClient entity = new OAuth2RegisteredClient();
        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        entity.setClientName(registeredClient.getClientName());
        entity.setClientAuthenticationMethods(
                new HashSet<>(
                        registeredClient
                                .getClientAuthenticationMethods()
                )
        );
        entity.setAuthorizationGrantTypes(
                new HashSet<>(
                        registeredClient
                                .getAuthorizationGrantTypes()
                )
        );
        entity.setRedirectUris(registeredClient.getRedirectUris());
        entity.setScopes(registeredClient.getScopes());
        entity.setClientSettings(registeredClient.getClientSettings());
        entity.setTokenSettings(registeredClient.getTokenSettings());
        return entity;
    }

    public static RegisteredClient convertRegisteredClient(OAuth2RegisteredClient oauth2RegisteredClient) {
        Set<ClientAuthenticationMethod> clientAuthenticationMethods = new HashSet<>(
                oauth2RegisteredClient
                        .getClientAuthenticationMethods()
        );

        Set<AuthorizationGrantType> authorizationGrantTypes = new HashSet<>(
                oauth2RegisteredClient
                        .getAuthorizationGrantTypes()
        );

        RegisteredClient.Builder builder = RegisteredClient.withId(oauth2RegisteredClient.getId())
                .clientId(oauth2RegisteredClient.getClientId())
                .clientSecret(oauth2RegisteredClient.getClientSecret())
                .clientName(oauth2RegisteredClient.getClientName())
                .clientAuthenticationMethods(methods -> methods.addAll(clientAuthenticationMethods))
                .authorizationGrantTypes(types -> types.addAll(authorizationGrantTypes))
                .redirectUris(uris -> uris.addAll(oauth2RegisteredClient.getRedirectUris()))
                .scopes(scopes -> scopes.addAll(oauth2RegisteredClient.getScopes()))
                .clientSettings(ClientSettings.withSettings(oauth2RegisteredClient.getClientSettings().getSettings()).build())
                .tokenSettings(TokenSettings.withSettings(oauth2RegisteredClient.getTokenSettings().getSettings()).build());

        Instant clientIdIssuedAt = oauth2RegisteredClient.getClientIdIssuedAt();
        if (clientIdIssuedAt != null) {
            builder.clientIdIssuedAt(clientIdIssuedAt);
        }

        Instant clientSecretExpiresAt = oauth2RegisteredClient.getClientSecretExpiresAt();
        if (clientSecretExpiresAt != null) {
            builder.clientSecretExpiresAt(clientSecretExpiresAt);
        }

        return builder.build();
    }
}
