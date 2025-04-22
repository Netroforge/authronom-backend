package com.github.netroforge.authronom_backend.db.repository.primary.entity;

import lombok.Data;

@Data
public class UserChangeEmailStartRequestDto {
    private String newEmail;
}
