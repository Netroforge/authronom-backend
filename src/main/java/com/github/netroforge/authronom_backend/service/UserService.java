package com.github.netroforge.authronom_backend.service;

import com.fasterxml.uuid.Generators;
import com.github.netroforge.authronom_backend.controller.dto.UserInfoResponseDto;
import com.github.netroforge.authronom_backend.db.repository.primary.entity.*;
import com.github.netroforge.authronom_backend.properties.UserRegistrationProperties;
import com.github.netroforge.authronom_backend.db.repository.primary.UserEmailVerificationRepository;
import com.github.netroforge.authronom_backend.db.repository.primary.UserRepository;
import com.github.netroforge.authronom_backend.service.task.UserEmailConfirmationSendTask;
import com.github.netroforge.authronom_backend.service.task.UserEmailConfirmationSendTaskData;
import com.github.netroforge.authronom_backend.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserRegistrationProperties userRegistrationProperties,
            DbschedulerService dbschedulerService,
            UserEmailVerificationRepository userEmailVerificationRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRegistrationProperties = userRegistrationProperties;
        this.dbschedulerService = dbschedulerService;
        this.userEmailVerificationRepository = userEmailVerificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserInfoResponseDto getUserInfo() {
        String userUid = SecurityUtils.getAuthorizedUserUid();
        User user = userRepository.findByUid(userUid);
        if (user == null) {
            log.error("User not found, this is not expected");
            throw new IllegalStateException("Something goes wrong");
        }
        return new UserInfoResponseDto(user.getUid(), user.getEmail());
    }

    public UserRegistrationStartResponseDto startUserRegistration(
            UserRegistrationStartRequestDto userRegistrationStartRequestDto
    ) {
        String confirmationCode = generateConfirmationCode();

        dbschedulerService.scheduleIfNotExists(
                UserEmailConfirmationSendTask.getTaskDescriptor()
                        .instance(Generators.timeBasedEpochGenerator().generate().toString())
                        .data(new UserEmailConfirmationSendTaskData(
                                userRegistrationStartRequestDto.getEmail(),
                                confirmationCode
                        ))
                        .scheduledTo(Instant.now())
        );
        return new UserRegistrationStartResponseDto(true);
    }

    public UserRegistrationFinalizeResponseDto finalizeUserRegistration(
            UserRegistrationFinalizeRequestDto userRegistrationFinalizeRequestDto
    ) {
        if (!userRegistrationProperties.isEmailEnabled()) {
            throw new IllegalStateException(
                    "Registration by email is disabled."
            );
        }

        // Do checks
        Assert.hasText(userRegistrationFinalizeRequestDto.getEmail(), "Please enter a valid email address.");
        Assert.isTrue(userRegistrationFinalizeRequestDto.getEmail().matches("^[^@]+@[^@]+$"), "Please enter a valid email address.");
        Assert.hasText(userRegistrationFinalizeRequestDto.getConfirmationCode(), "Please enter a valid confirmation code.");
        Assert.isTrue(userRegistrationFinalizeRequestDto.getConfirmationCode().matches("^[" + CONFIRMATION_CODE_CHARACTERS + "]+$"), "Please enter a valid confirmation code.");
        Assert.hasText(userRegistrationFinalizeRequestDto.getPassword(), "Password should be at least 6 characters.");
        Assert.isTrue(userRegistrationFinalizeRequestDto.getPassword().length() >= 6, "Password should be at least 6 characters.");

        // Check confirmation code
        UserEmailVerification userEmailVerification = userEmailVerificationRepository.findByEmailAndConfirmationCode(
                userRegistrationFinalizeRequestDto.getEmail(),
                userRegistrationFinalizeRequestDto.getConfirmationCode()
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
        try {
            User user = new User();
            user.setNew(true);
            user.setUid(Generators.timeBasedEpochGenerator().generate().toString());
            user.setEmail(userRegistrationFinalizeRequestDto.getEmail());
            user.setPassword(passwordEncoder.encode(userRegistrationFinalizeRequestDto.getPassword()));
            user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            userRepository.save(user);
        } catch (DbActionExecutionException dbActionExecutionException) {
            if (dbActionExecutionException.getCause() instanceof DuplicateKeyException) {
                throw new IllegalStateException("Email already in use.");
            } else {
                log.error("Error", dbActionExecutionException);
                throw new IllegalStateException("Something goes wrong.");
            }
        }

        userEmailVerificationRepository.deleteByEmailAndConfirmationCode(
                userRegistrationFinalizeRequestDto.getEmail(),
                userRegistrationFinalizeRequestDto.getConfirmationCode()
        );

        return new UserRegistrationFinalizeResponseDto(true);
    }

    public UserChangePasswordStartResponseDto startUserChangePassword() {
        String userUid = SecurityUtils.getAuthorizedUserUid();
        User user = userRepository.findByUid(userUid);
        if (user == null) {
            throw new IllegalStateException("Internal server error.");
        }

        String confirmationCode = generateConfirmationCode();

        dbschedulerService.scheduleIfNotExists(
                UserEmailConfirmationSendTask.getTaskDescriptor()
                        .instance(Generators.timeBasedEpochGenerator().generate().toString())
                        .data(new UserEmailConfirmationSendTaskData(
                                user.getEmail(),
                                confirmationCode
                        ))
                        .scheduledTo(Instant.now())
        );
        return new UserChangePasswordStartResponseDto(user.getEmail());
    }

    public UserChangePasswordFinalizeResponseDto finalizeUserChangePassword(
            UserChangePasswordFinalizeRequestDto userChangePasswordFinalizeRequestDto
    ) {
        String userUid = SecurityUtils.getAuthorizedUserUid();
        User user = userRepository.findByUid(userUid);
        if (user == null) {
            throw new IllegalStateException("Internal server error.");
        }

        // Do checks
        Assert.hasText(userChangePasswordFinalizeRequestDto.getConfirmationCode(), "Please enter a valid confirmation code.");
        Assert.isTrue(userChangePasswordFinalizeRequestDto.getConfirmationCode().matches("^[" + CONFIRMATION_CODE_CHARACTERS + "]+$"), "Please enter a valid confirmation code.");
        Assert.hasText(userChangePasswordFinalizeRequestDto.getNewPassword(), "Password should be at least 6 characters.");
        Assert.isTrue(userChangePasswordFinalizeRequestDto.getNewPassword().length() >= 6, "Password should be at least 6 characters.");

        // Check confirmation code
        UserEmailVerification userEmailVerification = userEmailVerificationRepository.findByEmailAndConfirmationCode(
                user.getEmail(),
                userChangePasswordFinalizeRequestDto.getConfirmationCode()
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

        // Update a user in the database
        user.setPassword(passwordEncoder.encode(userChangePasswordFinalizeRequestDto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        userEmailVerificationRepository.deleteByEmailAndConfirmationCode(
                user.getEmail(),
                userChangePasswordFinalizeRequestDto.getConfirmationCode()
        );

        return new UserChangePasswordFinalizeResponseDto(true);
    }


    public UserChangeEmailStartResponseDto startUserChangeEmail(
            UserChangeEmailStartRequestDto userChangeEmailStartRequestDto
    ) {
        String userUid = SecurityUtils.getAuthorizedUserUid();
        User user = userRepository.findByUid(userUid);
        if (user == null) {
            throw new IllegalStateException("Internal server error.");
        }

        // Validate new email
        Assert.hasText(userChangeEmailStartRequestDto.getNewEmail(), "Please enter a valid email address.");
        Assert.isTrue(userChangeEmailStartRequestDto.getNewEmail().matches("^[^@]+@[^@]+$"), "Please enter a valid email address.");

        // Generate confirmation codes for old email
        String oldEmailConfirmationCode = generateConfirmationCode();
        // Schedule email to old email address
        dbschedulerService.scheduleIfNotExists(
                UserEmailConfirmationSendTask.getTaskDescriptor()
                        .instance(Generators.timeBasedEpochGenerator().generate().toString())
                        .data(new UserEmailConfirmationSendTaskData(
                                user.getEmail(),
                                oldEmailConfirmationCode))
                        .scheduledTo(Instant.now()));

        // Generate confirmation codes for new email
        String newEmailConfirmationCode = generateConfirmationCode();
        // Schedule email to new email address
        dbschedulerService.scheduleIfNotExists(
                UserEmailConfirmationSendTask.getTaskDescriptor()
                        .instance(Generators.timeBasedEpochGenerator().generate().toString())
                        .data(new UserEmailConfirmationSendTaskData(
                                userChangeEmailStartRequestDto.getNewEmail(),
                                newEmailConfirmationCode))
                        .scheduledTo(Instant.now()));

        return new UserChangeEmailStartResponseDto(
                user.getEmail(),
                userChangeEmailStartRequestDto.getNewEmail()
        );
    }

    public UserChangeEmailFinalizeResponseDto finalizeUserChangeEmail(
            UserChangeEmailFinalizeRequestDto userChangeEmailFinalizeRequestDto
    ) {
        String userUid = SecurityUtils.getAuthorizedUserUid();
        User user = userRepository.findByUid(userUid);
        if (user == null) {
            throw new IllegalStateException("Internal server error.");
        }
        String oldEmail = user.getEmail();

        // Do checks
        Assert.hasText(userChangeEmailFinalizeRequestDto.getNewEmail(), "Please enter a valid email address.");
        Assert.isTrue(userChangeEmailFinalizeRequestDto.getNewEmail().matches("^[^@]+@[^@]+$"), "Please enter a valid email address.");
        Assert.hasText(userChangeEmailFinalizeRequestDto.getOldEmailConfirmationCode(), "Please enter a valid confirmation code for old email.");
        Assert.isTrue(userChangeEmailFinalizeRequestDto.getOldEmailConfirmationCode().matches("^[" + CONFIRMATION_CODE_CHARACTERS + "]+$"), "Please enter a valid confirmation code for old email.");
        Assert.hasText(userChangeEmailFinalizeRequestDto.getNewEmailConfirmationCode(), "Please enter a valid confirmation code for new email.");
        Assert.isTrue(userChangeEmailFinalizeRequestDto.getNewEmailConfirmationCode().matches("^[" + CONFIRMATION_CODE_CHARACTERS + "]+$"), "Please enter a valid confirmation code for new email.");

        // Check confirmation code for old email
        UserEmailVerification oldEmailVerification = userEmailVerificationRepository
                .findByEmailAndConfirmationCode(
                        user.getEmail(),
                        userChangeEmailFinalizeRequestDto.getOldEmailConfirmationCode()
                );
        if (oldEmailVerification == null) {
            throw new IllegalStateException(
                    "Wrong confirmation code provided for old email.");
        }
        if (Duration
                .between(
                        oldEmailVerification.getCreatedAt(),
                        LocalDateTime.now(ZoneOffset.UTC)
                )
                .compareTo(userRegistrationProperties.getConfirmationCodeLiveTime()) > 0) {
            throw new IllegalStateException(
                    "Confirmation code for old email expired.");
        }

        // Check confirmation code for new email
        UserEmailVerification newEmailVerification = userEmailVerificationRepository
                .findByEmailAndConfirmationCode(
                        userChangeEmailFinalizeRequestDto.getNewEmail(),
                        userChangeEmailFinalizeRequestDto.getNewEmailConfirmationCode());
        if (newEmailVerification == null) {
            throw new IllegalStateException(
                    "Wrong confirmation code provided for new email.");
        }
        if (Duration
                .between(
                        newEmailVerification.getCreatedAt(),
                        LocalDateTime.now(ZoneOffset.UTC)
                )
                .compareTo(userRegistrationProperties.getConfirmationCodeLiveTime()) > 0) {
            throw new IllegalStateException(
                    "Confirmation code for new email expired.");
        }

        // Update user's email
        user.setEmail(userChangeEmailFinalizeRequestDto.getNewEmail());
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        // Cleanup verification records
        userEmailVerificationRepository.deleteByEmailAndConfirmationCode(
                oldEmail,
                userChangeEmailFinalizeRequestDto.getOldEmailConfirmationCode()
        );
        userEmailVerificationRepository.deleteByEmailAndConfirmationCode(
                userChangeEmailFinalizeRequestDto.getNewEmail(),
                userChangeEmailFinalizeRequestDto.getNewEmailConfirmationCode()
        );

        return new UserChangeEmailFinalizeResponseDto(true);
    }

    private String generateConfirmationCode() {
        // Generate a random number with the specified number of digits
        // Inspired by https://dev.to/dev_eliud/implementing-email-code-verification-in-java-spring-boot-2c18
        StringBuilder confirmationCodeBuilder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            confirmationCodeBuilder.append(
                    CONFIRMATION_CODE_CHARACTERS
                            .charAt(SECURE_RANDOM.nextInt(CONFIRMATION_CODE_CHARACTERS.length())));
        }
        return confirmationCodeBuilder.toString();
    }
}
