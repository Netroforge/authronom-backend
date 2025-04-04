package com.github.netroforge.authronom_backend.service;

import com.fasterxml.uuid.Generators;
import com.github.netroforge.authronom_backend.properties.UserRegistrationProperties;
import com.github.netroforge.authronom_backend.repository.UserEmailVerificationRepository;
import com.github.netroforge.authronom_backend.repository.UserRepository;
import com.github.netroforge.authronom_backend.repository.entity.*;
import com.github.netroforge.authronom_backend.service.task.UserEmailConfirmationSendTask;
import com.github.netroforge.authronom_backend.service.task.UserEmailConfirmationSendTaskData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class UserService {
    private static final String CONFIRMATION_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserRegistrationProperties userRegistrationProperties;
    private final DbschedulerService dbschedulerService;
    private final UserEmailVerificationRepository userEmailVerificationRepository;

    public UserService(
            UserRepository userRepository,
            UserRegistrationProperties userRegistrationProperties,
            DbschedulerService dbschedulerService,
            UserEmailVerificationRepository userEmailVerificationRepository
    ) {
        this.userRepository = userRepository;
        this.userRegistrationProperties = userRegistrationProperties;
        this.dbschedulerService = dbschedulerService;
        this.userEmailVerificationRepository = userEmailVerificationRepository;
    }

    public UserStartRegistrationResponseDto startUserRegistration(
            UserStartRegistrationRequestDto userStartRegistrationRequestDto
    ) {
        // Generate a random number with the specified number of digits
        // Inspired by https://dev.to/dev_eliud/implementing-email-code-verification-in-java-spring-boot-2c18
        StringBuilder confirmationCodeBuilder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            confirmationCodeBuilder.append(
                    CONFIRMATION_CODE_CHARACTERS
                            .charAt(SECURE_RANDOM.nextInt(CONFIRMATION_CODE_CHARACTERS.length())))
            ;
        }
        String confirmationCode = confirmationCodeBuilder.toString();

        dbschedulerService.scheduleIfNotExists(
                UserEmailConfirmationSendTask.getTaskDescriptor()
                        .instance(Generators.timeBasedEpochGenerator().generate().toString())
                        .data(new UserEmailConfirmationSendTaskData(
                                userStartRegistrationRequestDto.getEmail(),
                                confirmationCode
                        ))
                        .scheduledTo(Instant.now())
        );
        return new UserStartRegistrationResponseDto(true);
    }

    public UserFinalizeRegistrationResponseDto finalizeUserRegistration(
            UserFinalizeRegistrationRequestDto userFinalizeRegistrationRequestDto
    ) {
        if (!userRegistrationProperties.isEmailEnabled()) {
            throw new IllegalStateException(
                    "Registration by email is disabled."
            );
        }

        // Do checks
        Assert.hasText(userFinalizeRegistrationRequestDto.getEmail(), "Please enter a valid email address.");
        Assert.isTrue(userFinalizeRegistrationRequestDto.getEmail().matches("^[^@]+@[^@]+$"), "Please enter a valid email address.");
        Assert.hasText(userFinalizeRegistrationRequestDto.getConfirmationCode(), "Please enter a valid confirmation code.");
        Assert.isTrue(userFinalizeRegistrationRequestDto.getConfirmationCode().matches("^[" + CONFIRMATION_CODE_CHARACTERS + "]+$"), "Please enter a valid confirmation code.");
        Assert.hasText(userFinalizeRegistrationRequestDto.getPassword(), "Password should be at least 6 characters.");
        Assert.isTrue(userFinalizeRegistrationRequestDto.getPassword().length() >= 6, "Password should be at least 6 characters.");

        // Check confirmation code
        UserEmailVerification userEmailVerification = userEmailVerificationRepository.findByEmailAndConfirmationCode(
                userFinalizeRegistrationRequestDto.getEmail(),
                userFinalizeRegistrationRequestDto.getConfirmationCode()
        );
        if (userEmailVerification == null) {
            throw new IllegalStateException(
                    "Wrong confirmation code provided."
            );
        }
        if (Duration.between(
                userEmailVerification.getCreatedAt(),
                LocalDateTime.now(ZoneOffset.UTC)
        ).compareTo(userRegistrationProperties.getConfirmationCodeLiveTime()) > 0) {
            throw new IllegalStateException(
                    "Confirmation code expired."
            );
        }

        // Create a new user in the database
        User user = new User();
        user.setNew(true);
        user.setUid(Generators.timeBasedEpochGenerator().generate().toString());
        user.setEmail(userFinalizeRegistrationRequestDto.getEmail());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(userFinalizeRegistrationRequestDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        userEmailVerificationRepository.deleteByEmailAndConfirmationCode(
                userFinalizeRegistrationRequestDto.getEmail(),
                userFinalizeRegistrationRequestDto.getConfirmationCode()
        );

        return new UserFinalizeRegistrationResponseDto(true);
    }

    public UserLoginResponseDto login(
            UserLoginRequestDto userLoginRequestDto
    ) {

        return new UserLoginResponseDto(true);
    }
}
