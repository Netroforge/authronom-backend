package com.github.netroforge.authronom_backend.service;

import com.fasterxml.uuid.Generators;
import com.github.netroforge.authronom_backend.repository.db.UserRepository;
import com.github.netroforge.authronom_backend.repository.db.entity.User;
import com.github.netroforge.authronom_backend.service.dto.CustomOidcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        if ("google".equals(userRequest.getClientRegistration().getRegistrationId())) {
            String googleId = oidcUser.getAttribute("sub");
            User user = userRepository.findByGoogleId(googleId);
            if (user == null) {
                log.debug(
                        "Saving first time user: name={}, claims={}, authorities={}",
                        oidcUser.getName(),
                        oidcUser.getAttributes(),
                        oidcUser.getAuthorities()
                );
                User userByEmail = userRepository.findByEmail(oidcUser.getAttribute("email"));
                if (userByEmail != null) {
                    userByEmail.setGoogleId(googleId);
                    userByEmail.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    user = userRepository.save(userByEmail);
                } else {
                    user = new User();
                    user.setUid(Generators.timeBasedEpochGenerator().generate().toString());
                    user.setEmail(oidcUser.getAttribute("email"));
                    user.setGoogleId(googleId);
                    user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    user.setNew(true);
                    user = userRepository.save(user);
                }
            }

            return new CustomOidcUser(
                    user.getUid(),
                    oidcUser.getAuthorities(),
                    oidcUser.getAttributes(),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo()
            );
        } else {
            throw new IllegalStateException("Unsupported registration id: " + userRequest.getClientRegistration().getRegistrationId());
        }
    }
}
