package com.github.netroforge.authronom_backend.controller;

import com.github.netroforge.authronom_backend.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class TestController {

    @GetMapping("/auth/test")
    public String test() {
        String userUid = SecurityUtils.getAuthorizedUserUid();
        return "user uid: '" + userUid + "', random number: '" + ThreadLocalRandom.current().nextLong() + "'";
    }
}
