package com.github.netroforge.authronom_backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationInfo implements Serializable {
    private String clientId;
    private LocalDateTime startDate;
    private LocalDateTime lastRefreshDate;
    private Set<String> scopes;
    private AuthorizationGrantType authorizationGrantType;
    private String authorizationId;
    private String userUid;
    private String redirectUri;
}
