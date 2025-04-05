package com.github.netroforge.authronom_backend.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordFinalizeRequestDto {
    private String confirmationCode;
    private String newPassword;
}
