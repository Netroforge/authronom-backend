package com.github.netroforge.authronom_backend.repository.entity;

import lombok.Data;

@Data
public class UserChangeEmailStartRequestDto {
    private String newEmail;
}
