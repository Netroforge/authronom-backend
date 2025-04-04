package com.github.netroforge.authronom_backend.service.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEmailConfirmationSendTaskData implements Serializable {
    private String email;
    private String confirmationCode;
}

