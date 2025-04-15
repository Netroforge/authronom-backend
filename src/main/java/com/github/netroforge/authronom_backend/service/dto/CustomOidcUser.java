package com.github.netroforge.authronom_backend.service.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class CustomOidcUser implements AuthorizedUser, OidcUser, Serializable {
    private final String uid;

    private final Collection<? extends GrantedAuthority> authorities;

    private final Map<String, Object> attributes;

    private final OidcIdToken oidcIdToken;

    private final OidcUserInfo oidcUserInfo;

    public CustomOidcUser(
            String uid,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            OidcIdToken oidcIdToken,
            OidcUserInfo oidcUserInfo
    ) {
        this.uid = uid;
        this.authorities = authorities;
        this.attributes = attributes;
        this.oidcIdToken = oidcIdToken;
        this.oidcUserInfo = oidcUserInfo;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUserInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcIdToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return uid;
    }
}
