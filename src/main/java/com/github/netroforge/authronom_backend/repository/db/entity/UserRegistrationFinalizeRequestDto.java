package com.github.netroforge.authronom_backend.repository.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationFinalizeRequestDto {
    private String email;
    private String confirmationCode;
    private String password;
}
