package com.github.netroforge.authronom_backend.service;

import com.fasterxml.uuid.Generators;
import com.github.netroforge.authronom_backend.repository.UserRepository;
import com.github.netroforge.authronom_backend.repository.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public final class UserRepositoryOAuth2UserHandler {

    private final UserRepository userRepository;

    public UserRepositoryOAuth2UserHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void accept(
            OAuth2User oAuth2User,
            String authorizedClientRegistrationId
    ) {
        // Capture user in a local data store on first authentication
        log.info("UserRepositoryOAuth2UserHandler {}", oAuth2User);

        if ("google".equals(authorizedClientRegistrationId)) {
            log.info(
                    "Saving first time user: name={}, claims={}, authorities={}",
                    oAuth2User.getName(),
                    oAuth2User.getAttributes(),
                    oAuth2User.getAuthorities()
            );
            String googleId = oAuth2User.getAttribute("sub");
            User user = userRepository.findByGoogleId(googleId);
            if (user == null) {
                User userByEmail = userRepository.findByEmail(oAuth2User.getAttribute("email"));
                if (userByEmail != null) {
                    userByEmail.setGoogleId(googleId);
					userByEmail.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    userRepository.save(userByEmail);
                } else {
                    user = new User();
                    user.setUid(Generators.timeBasedEpochGenerator().generate().toString());
                    user.setEmail(oAuth2User.getAttribute("email"));
                    user.setGoogleId(googleId);
                    user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    user.setNew(true);
                    userRepository.save(user);
                }
            }
        }
    }
}
