package com.github.netroforge.authronom_backend.db.repository.primary.entity;

import lombok.Data;

@Data
public class UserChangeEmailFinalizeRequestDto {
    private String newEmail;
    private String oldEmailConfirmationCode;
    private String newEmailConfirmationCode;
}
