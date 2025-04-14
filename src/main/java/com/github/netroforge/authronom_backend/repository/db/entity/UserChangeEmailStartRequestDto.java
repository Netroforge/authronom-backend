package com.github.netroforge.authronom_backend.repository.db.entity;

import lombok.Data;

@Data
public class UserChangeEmailStartRequestDto {
    private String newEmail;
}
