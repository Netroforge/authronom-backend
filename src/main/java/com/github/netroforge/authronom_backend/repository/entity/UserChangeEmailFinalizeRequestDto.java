package com.github.netroforge.authronom_backend.repository.entity;

import lombok.Data;

@Data
public class UserChangeEmailFinalizeRequestDto {
    private String newEmail;
    private String oldEmailConfirmationCode;
    private String newEmailConfirmationCode;
}
