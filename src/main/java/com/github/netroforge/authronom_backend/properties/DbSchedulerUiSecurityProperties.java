package com.github.netroforge.authronom_backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "db-scheduler-ui.security")
public class DbSchedulerUiSecurityProperties {
    private String adminUsername;
    private String adminPassword;
}

