package com.github.netroforge.authronom_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class AuthronomBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthronomBackendApplication.class, args);
    }
}