package com.github.netroforge.authronom_backend.db.repository.entity;

import lombok.Data;

@Data
public class UserChangeEmailStartRequestDto {
    private String newEmail;
}
