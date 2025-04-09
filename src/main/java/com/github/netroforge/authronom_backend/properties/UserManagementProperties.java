package com.github.netroforge.authronom_backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "user.management")
public class UserManagementProperties {
    private boolean emailEnabled;
    private Duration danglingConfirmationCodeDeletionInterval;
    private Duration danglingConfirmationCodeRetentionThreshold;
    private Duration confirmationCodeLiveTime;
    private String confirmationEmailFrom;
    private String confirmationEmailFromName;
    private String confirmationEmailSubject;
    private int startUserRegistrationCallsRateLimitWithinSecond;
    private int finalizeUserRegistrationCallsRateLimitWithinSecond;
}
