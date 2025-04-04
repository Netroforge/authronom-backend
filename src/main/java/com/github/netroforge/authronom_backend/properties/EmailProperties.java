package com.github.netroforge.authronom_backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    private String server;
    private int port;
    private boolean ssl;
    private String username;
    private String password;
}
