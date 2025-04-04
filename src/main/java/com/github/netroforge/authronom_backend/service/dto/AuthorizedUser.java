package com.github.netroforge.authronom_backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class AuthorizedUser extends User implements OAuth2User {
    private String uid;
    private Map<String, Object> oauthAttributes;

    public AuthorizedUser(
            String uid,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> oauthAttributes
    ) {
        super(
                email,
                password,
                authorities
        );
        this.uid = uid;
        this.oauthAttributes = oauthAttributes;
    }

    public String getEmail() {
        return this.getUsername();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauthAttributes;
    }

    @Override
    public String getName() {
        return this.getUsername();
    }
}
