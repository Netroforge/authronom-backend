package com.github.netroforge.authronom_backend.repository.redis.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("oauth2_registered_client")
public class OAuth2RegisteredClient {
    @Id
    private String id;

    @Indexed
    private String clientId;

    private Instant clientIdIssuedAt;

    private String clientSecret;

    private Instant clientSecretExpiresAt;

    private String clientName;

    private Set<ClientAuthenticationMethod> clientAuthenticationMethods;

    private Set<AuthorizationGrantType> authorizationGrantTypes;

    private Set<String> redirectUris;

    private Set<String> postLogoutRedirectUris;

    private Set<String> scopes;

    private ClientSettings clientSettings;

    private TokenSettings tokenSettings;

//    public static class ClientSettings {
//
//        private final boolean requireProofKey;
//
//        private final boolean requireAuthorizationConsent;
//
//        private final String jwkSetUrl;
//
//        private final JwsAlgorithm tokenEndpointAuthenticationSigningAlgorithm;
//
//        private final String x509CertificateSubjectDN;
//
//        public ClientSettings(boolean requireProofKey, boolean requireAuthorizationConsent, String jwkSetUrl,
//                              JwsAlgorithm tokenEndpointAuthenticationSigningAlgorithm, String x509CertificateSubjectDN) {
//            this.requireProofKey = requireProofKey;
//            this.requireAuthorizationConsent = requireAuthorizationConsent;
//            this.jwkSetUrl = jwkSetUrl;
//            this.tokenEndpointAuthenticationSigningAlgorithm = tokenEndpointAuthenticationSigningAlgorithm;
//            this.x509CertificateSubjectDN = x509CertificateSubjectDN;
//        }
//
//        public boolean isRequireProofKey() {
//            return this.requireProofKey;
//        }
//
//        public boolean isRequireAuthorizationConsent() {
//            return this.requireAuthorizationConsent;
//        }
//
//        public String getJwkSetUrl() {
//            return this.jwkSetUrl;
//        }
//
//        public JwsAlgorithm getTokenEndpointAuthenticationSigningAlgorithm() {
//            return this.tokenEndpointAuthenticationSigningAlgorithm;
//        }
//
//        public String getX509CertificateSubjectDN() {
//            return this.x509CertificateSubjectDN;
//        }
//
//    }
//
//    public static class TokenSettings {
//
//        private final Duration authorizationCodeTimeToLive;
//
//        private final Duration accessTokenTimeToLive;
//
//        private final OAuth2TokenFormat accessTokenFormat;
//
//        private final Duration deviceCodeTimeToLive;
//
//        private final boolean reuseRefreshTokens;
//
//        private final Duration refreshTokenTimeToLive;
//
//        private final SignatureAlgorithm idTokenSignatureAlgorithm;
//
//        private final boolean x509CertificateBoundAccessTokens;
//
//        public TokenSettings(Duration authorizationCodeTimeToLive, Duration accessTokenTimeToLive,
//                             OAuth2TokenFormat accessTokenFormat, Duration deviceCodeTimeToLive, boolean reuseRefreshTokens,
//                             Duration refreshTokenTimeToLive, SignatureAlgorithm idTokenSignatureAlgorithm,
//                             boolean x509CertificateBoundAccessTokens) {
//            this.authorizationCodeTimeToLive = authorizationCodeTimeToLive;
//            this.accessTokenTimeToLive = accessTokenTimeToLive;
//            this.accessTokenFormat = accessTokenFormat;
//            this.deviceCodeTimeToLive = deviceCodeTimeToLive;
//            this.reuseRefreshTokens = reuseRefreshTokens;
//            this.refreshTokenTimeToLive = refreshTokenTimeToLive;
//            this.idTokenSignatureAlgorithm = idTokenSignatureAlgorithm;
//            this.x509CertificateBoundAccessTokens = x509CertificateBoundAccessTokens;
//        }
//
//        public Duration getAuthorizationCodeTimeToLive() {
//            return this.authorizationCodeTimeToLive;
//        }
//
//        public Duration getAccessTokenTimeToLive() {
//            return this.accessTokenTimeToLive;
//        }
//
//        public OAuth2TokenFormat getAccessTokenFormat() {
//            return this.accessTokenFormat;
//        }
//
//        public Duration getDeviceCodeTimeToLive() {
//            return this.deviceCodeTimeToLive;
//        }
//
//        public boolean isReuseRefreshTokens() {
//            return this.reuseRefreshTokens;
//        }
//
//        public Duration getRefreshTokenTimeToLive() {
//            return this.refreshTokenTimeToLive;
//        }
//
//        public SignatureAlgorithm getIdTokenSignatureAlgorithm() {
//            return this.idTokenSignatureAlgorithm;
//        }
//
//        public boolean isX509CertificateBoundAccessTokens() {
//            return this.x509CertificateBoundAccessTokens;
//        }
//
//    }

}
