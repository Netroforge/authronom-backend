package com.github.netroforge.authronom_backend.properties;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    @NotEmpty(message = "allowed-origins or allowed-origin-patterns must not be empty")
    private List<String> allowedOrigins;
    private List<String> allowedOriginPatterns;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private List<String> exposedHeaders;
    private boolean allowCredentials;

    /**
     * Max age for CORS preflight requests.
     */
    private Long maxAge;
}

