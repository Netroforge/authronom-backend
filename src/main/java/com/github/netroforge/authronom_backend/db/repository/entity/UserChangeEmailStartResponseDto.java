package com.github.netroforge.authronom_backend.db.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangeEmailStartResponseDto {
    private String oldEmail;
    private String newEmail;
}
