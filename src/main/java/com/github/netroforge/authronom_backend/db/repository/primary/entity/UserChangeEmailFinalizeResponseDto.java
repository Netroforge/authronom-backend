package com.github.netroforge.authronom_backend.db.repository.primary.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangeEmailFinalizeResponseDto {
    private boolean processed;
}
