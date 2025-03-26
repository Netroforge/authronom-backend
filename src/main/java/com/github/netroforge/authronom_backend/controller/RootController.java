package com.github.netroforge.authronom_backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class RootController {

    @GetMapping("/")
    public Long test(Authentication authentication) {
        return ThreadLocalRandom.current().nextLong();
    }
}
