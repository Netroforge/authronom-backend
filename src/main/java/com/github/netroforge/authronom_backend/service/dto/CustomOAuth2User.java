package com.github.netroforge.authronom_backend.service.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements AuthorizedUser, OAuth2User, Serializable {
    private final String uid;

    private final Collection<? extends GrantedAuthority> authorities;

    private final Map<String, Object> attributes;

    public CustomOAuth2User(
            String uid,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes
    ) {
        this.uid = uid;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    @Override
    public String getUid() {
        return uid;
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
