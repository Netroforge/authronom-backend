package com.github.netroforge.authronom_backend.service;

import com.github.netroforge.authronom_backend.db.repository.UserRepository;
import com.github.netroforge.authronom_backend.db.repository.entity.User;
import com.github.netroforge.authronom_backend.service.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new BadCredentialsException("Bad credentials");
        }
        return new CustomUserDetails(
                user.getUid(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
