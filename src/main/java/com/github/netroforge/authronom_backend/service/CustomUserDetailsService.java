package com.github.netroforge.authronom_backend.service;

import com.github.netroforge.authronom_backend.repository.UserRepository;
import com.github.netroforge.authronom_backend.repository.entity.User;
import com.github.netroforge.authronom_backend.service.dto.AuthorizedUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.Collections;
import java.util.Map;

public class CustomUserDetailsService implements UserDetailsManager, UserDetailsPasswordService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User with username = " + username + " not found");
        }
        return new AuthorizedUser(
                user.getUid(),
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList(),
                Map.of(
                        "email", user.getEmail()
                )
        );
    }

    @Override
    public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("User with username = " + userDetails.getUsername() + " not found");
        }
        return new AuthorizedUser(
                user.getUid(),
                user.getEmail(),
                newPassword,
                Collections.emptyList(),
                Map.of(
                        "email", user.getEmail()
                )
        );
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }
}
