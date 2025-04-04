package com.github.netroforge.authronom_backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    // Main login page
    private String loginUrlAuthenticationEntryPoint;

    // Form login
    private String formLoginPage;
    private String formLoginFailureUrl;
    private String formLoginProcessingPath;
    private int bcryptPasswordEncoderStrength;

    // OAuth2 login
    private String oauth2LoginPage;
    private String oauth2LoginSuccessUrl;
    private String oauth2LoginFailureUrl;
}
