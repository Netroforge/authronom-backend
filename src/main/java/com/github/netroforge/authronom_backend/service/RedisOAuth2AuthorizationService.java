package com.github.netroforge.authronom_backend.service;

import com.github.netroforge.authronom_backend.service.dto.AuthorizationInfo;
import com.github.netroforge.authronom_backend.service.dto.AuthorizedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
public final class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {
    public final static String principalAttributeKey = "java.security.Principal";

    /**
     * Key prefix for OAuth2Authorization objects that already have an access token.
     * i.e. those that are completed
     */
    private final static String COMPLETE_KEY_PREFIX = "oauth2_authorization_complete:";

    /**
     * Key prefix for OAuth2Authorization objects for which the authorization process has not yet completed
     * and there is no access token yet. This situation can occur during the authorization code flow,
     * at the stage of working with the authorization code. Before requesting access tokens.
     */
    private final static String INIT_KEY_PREFIX = "oauth2_authorization_init:";

    /**
     * Key prefix for extended information about OAuth2Authorization object.
     */
    private final static String INFO_KEY_PREFIX = "oauth2_authorization_info:";

    private final RedisTemplate<String, OAuth2Authorization> redisTemplate;
    private final RedisTemplate<String, AuthorizationInfo> redisTemplateAuthInfo;
    private final ValueOperations<String, OAuth2Authorization> authorizations;
    private final ValueOperations<String, AuthorizationInfo> authInfoByUser;
    private final Duration ttl;

    /**
     * @param redisTemplate         Redis client through which OAuth2Authorization objects will be stored
     * @param redisTemplateAuthInfo Redis client through which AuthorizationInfo objects will be stored
     * @param ttl                   Time To Live for the record
     */
    public RedisOAuth2AuthorizationService(
            RedisTemplate<String, OAuth2Authorization> redisTemplate,
            RedisTemplate<String, AuthorizationInfo> redisTemplateAuthInfo,
            Duration ttl
    ) {
        this.redisTemplate = redisTemplate;
        this.authorizations = redisTemplate.opsForValue();
        this.redisTemplateAuthInfo = redisTemplateAuthInfo;
        this.authInfoByUser = redisTemplateAuthInfo.opsForValue();
        this.ttl = ttl;
    }

    /**
     * Get Principal object from {@link OAuth2Authorization}
     */
    private static AuthorizedUser extractPrincipal(OAuth2Authorization authorization) {
        AuthorizedUser authorizedUser = null;
        if (authorization.getAttributes().containsKey(principalAttributeKey)) {
            Authentication userAuthentication = authorization.getAttribute(principalAttributeKey);
            if (userAuthentication.getPrincipal() != null) {
                if (userAuthentication.getPrincipal() instanceof AuthorizedUser principal) {
                    authorizedUser = principal;
                } else {
                    log.warn(
                            "Principal object of type {} isn't supported",
                            userAuthentication.getPrincipal().getClass().getName()
                    );
                }
            }
        }
        return authorizedUser;
    }

    private static boolean isComplete(OAuth2Authorization authorization) {
        return authorization.getAccessToken() != null;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        if (isComplete(authorization)) {
            String key = COMPLETE_KEY_PREFIX + authorization.getId();
            AuthorizationInfo info = saveAuthInfo(authorization);

            String initKey = INIT_KEY_PREFIX + authorization.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(initKey))) {
                redisTemplate.delete(initKey);
            }
            authorizations.set(key, authorization, ttl);
        } else {
            String key = INIT_KEY_PREFIX + authorization.getId();
            authorizations.set(key, authorization, ttl);
        }
    }

    /**
     * Saving extended information about OAuth2Authorization object.
     */
    private AuthorizationInfo saveAuthInfo(OAuth2Authorization authorization) {
        AuthorizedUser authorizedUser = extractPrincipal(authorization);

        String redirectUri = null;
        OAuth2AuthorizationRequest authRequest = authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
        if (authRequest != null) {
            redirectUri = authRequest.getRedirectUri();
        }

        String key;
        if (authorizedUser != null) {
            key = INFO_KEY_PREFIX
                    + authorizedUser.getUid()
                    + ":" + authorization.getId();
        } else {
            key = INFO_KEY_PREFIX
                    + authorization.getPrincipalName()
                    + ":" + authorization.getId();
        }

        boolean keyAlreadyExists = Boolean.TRUE.equals(redisTemplateAuthInfo.hasKey(key));
        AuthorizationInfo lastAuthInfo = null;
        if (keyAlreadyExists) {
            lastAuthInfo = this.authInfoByUser.get(key);
        }

        AuthorizationInfo authorizationInfo = new AuthorizationInfo(
                authorization.getRegisteredClientId(),
                lastAuthInfo != null ? lastAuthInfo.getStartDate() : LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC),
                authorization.getAuthorizedScopes(),
                authorization.getAuthorizationGrantType(),
                authorization.getId(),
                authorizedUser != null ? authorizedUser.getUid() : null,
                redirectUri
        );
        this.authInfoByUser.set(key, authorizationInfo, ttl);
        return authorizationInfo;
    }

    /**
     * Removes authorization information. Only for OAuth2Authorization objects
     * that have completed the authorization process.
     */
    public void remove(String authorizationId) {
        String key = COMPLETE_KEY_PREFIX + authorizationId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            OAuth2Authorization completeAuthorization = this.authorizations.get(key);
            this.remove(completeAuthorization);
        }
    }

    /**
     * Deleting authorization information.
     */
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        if (isComplete(authorization)) {
            String key = COMPLETE_KEY_PREFIX + authorization.getId();
            AuthorizationInfo info = this.deleteAuthorizationInfo(authorization);

            // notify about token deletion
            this.redisTemplate.delete(key);
        } else {
            String key = INIT_KEY_PREFIX + authorization.getId();
            this.redisTemplate.delete(key);
        }
    }

    /**
     * Deleting additional authorization information.
     */
    private AuthorizationInfo deleteAuthorizationInfo(OAuth2Authorization authorization) {
        AuthorizedUser authorizedUser = extractPrincipal(authorization);
        String key;
        if (authorizedUser != null) {
            key = INFO_KEY_PREFIX
                    + authorizedUser.getUid()
                    + ":" + authorization.getId();
        } else {
            key = INFO_KEY_PREFIX
                    + authorization.getPrincipalName()
                    + ":" + authorization.getId();
        }
        AuthorizationInfo tokenDto = this.authInfoByUser.get(key);
        this.redisTemplateAuthInfo.delete(key);
        return tokenDto;
    }

    /**
     * Search by authorization ID
     */
    @Nullable
    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        OAuth2Authorization completeAuthorization = this.authorizations.get(COMPLETE_KEY_PREFIX + id);
        if (completeAuthorization != null) {
            return completeAuthorization;
        } else {
            return this.authorizations.get(INIT_KEY_PREFIX + id);
        }
    }

    /**
     * Search by access token
     */
    @Nullable
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        OAuth2Authorization authorization = findByToken(token, tokenType, COMPLETE_KEY_PREFIX);

        if (authorization == null) {
            authorization = findByToken(token, tokenType, INIT_KEY_PREFIX);
        }
        return authorization;
    }

    /**
     * Search for OAuth2Authorization object by access token and key prefix.
     * Key prefixes are stored in the constants of this class.
     */
    private OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType, String prefixKey) {
        Set<String> allInitKeys = redisTemplate.keys(prefixKey + "*");
        if (allInitKeys != null) {
            for (String authorizationKey : allInitKeys) {
                OAuth2Authorization authorization = this.authorizations.get(authorizationKey);
                if (hasToken(authorization, token, tokenType)) {
                    return authorization;
                }
            }
        }
        return null;
    }

    /**
     * Search for user's token information by their ID.
     */
    public List<AuthorizationInfo> findInfoByUserId(UUID userId) {
        Set<String> allKeys = redisTemplateAuthInfo.keys(INFO_KEY_PREFIX + userId + ":*");
        List<AuthorizationInfo> result = new ArrayList<>();
        if (allKeys != null) {
            for (String key : allKeys) {
                AuthorizationInfo info = this.authInfoByUser.get(key);
                result.add(info);
            }
        }
        return result;
    }

    private boolean hasToken(
            OAuth2Authorization authorization,
            String token,
            @Nullable OAuth2TokenType tokenType
    ) {
        if (tokenType == null) {
            return matchesState(authorization, token) ||
                    matchesAuthorizationCode(authorization, token) ||
                    matchesAccessToken(authorization, token) ||
                    matchesIdToken(authorization, token) ||
                    matchesRefreshToken(authorization, token) ||
                    matchesDeviceCode(authorization, token) ||
                    matchesUserCode(authorization, token);
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            return matchesState(authorization, token);
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            return matchesAuthorizationCode(authorization, token);
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            return matchesAccessToken(authorization, token);
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            return matchesIdToken(authorization, token);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            return matchesRefreshToken(authorization, token);
        } else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            return matchesDeviceCode(authorization, token);
        } else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            return matchesUserCode(authorization, token);
        }
        return false;
    }

    private boolean matchesState(OAuth2Authorization authorization, String token) {
        return token.equals(authorization.getAttribute(OAuth2ParameterNames.STATE));
    }

    private boolean matchesAuthorizationCode(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        return authorizationCode != null && authorizationCode.getToken().getTokenValue().equals(token);
    }

    private boolean matchesAccessToken(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
                authorization.getToken(OAuth2AccessToken.class);
        return accessToken != null && accessToken.getToken().getTokenValue().equals(token);
    }

    private boolean matchesRefreshToken(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                authorization.getToken(OAuth2RefreshToken.class);
        return refreshToken != null && refreshToken.getToken().getTokenValue().equals(token);
    }

    private boolean matchesIdToken(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OidcIdToken> idToken =
                authorization.getToken(OidcIdToken.class);
        return idToken != null && idToken.getToken().getTokenValue().equals(token);
    }

    private boolean matchesDeviceCode(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode =
                authorization.getToken(OAuth2DeviceCode.class);
        return deviceCode != null && deviceCode.getToken().getTokenValue().equals(token);
    }

    private boolean matchesUserCode(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2UserCode> userCode =
                authorization.getToken(OAuth2UserCode.class);
        return userCode != null && userCode.getToken().getTokenValue().equals(token);
    }
}
