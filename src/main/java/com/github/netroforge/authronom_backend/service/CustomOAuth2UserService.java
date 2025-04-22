package com.github.netroforge.authronom_backend.service;

import com.fasterxml.uuid.Generators;
import com.github.netroforge.authronom_backend.db.repository.UserRepository;
import com.github.netroforge.authronom_backend.db.repository.entity.User;
import com.github.netroforge.authronom_backend.service.dto.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        if ("google".equals(userRequest.getClientRegistration().getRegistrationId())) {
            return getCustomOAuth2UserFromGoogle(oAuth2User);
        } else {
            throw new IllegalStateException("Unsupported registration id: " + userRequest.getClientRegistration().getRegistrationId());
        }
    }

    private CustomOAuth2User getCustomOAuth2UserFromGoogle(OAuth2User oAuth2User) {
        String googleId = oAuth2User.getAttribute("sub");
        User user = userRepository.findByGoogleId(googleId);
        if (user == null) {
            log.debug(
                    "Saving first time user: name={}, claims={}, authorities={}",
                    oAuth2User.getName(),
                    oAuth2User.getAttributes(),
                    oAuth2User.getAuthorities()
            );
            User userByEmail = userRepository.findByEmail(oAuth2User.getAttribute("email"));
            if (userByEmail != null) {
                userByEmail.setGoogleId(googleId);
                userByEmail.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                user = userRepository.save(userByEmail);
            } else {
                user = new User();
                user.setUid(Generators.timeBasedEpochGenerator().generate().toString());
                user.setEmail(oAuth2User.getAttribute("email"));
                user.setGoogleId(googleId);
                user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                user.setNew(true);
                user = userRepository.save(user);
            }
        }

        return new CustomOAuth2User(
                user.getUid(),
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes()
        );
    }
}
