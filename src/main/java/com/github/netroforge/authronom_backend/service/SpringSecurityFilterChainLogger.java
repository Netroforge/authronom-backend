package com.github.netroforge.authronom_backend.service;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SpringSecurityFilterChainLogger implements ApplicationRunner {
    private final FilterChainProxy filterChainProxy;

    public SpringSecurityFilterChainLogger(FilterChainProxy filterChainProxy) {
        this.filterChainProxy = filterChainProxy;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        StringBuilder output = new StringBuilder();
        output.append("Spring Security Filter Chains:");
        List<SecurityFilterChain> securityFilterChainList = filterChainProxy.getFilterChains();
        for (int i = 0; i < securityFilterChainList.size(); i++) {
            SecurityFilterChain securityFilterChain = securityFilterChainList.get(i);
            output.append("\n Filter Chain #").append(i + 1);
            output.append("\n  Request Matcher: ").append(securityFilterChain); // Prints the matcher details
            output.append("\n  Filters:");
            for (Filter filter : securityFilterChain.getFilters()) {
                output.append("\n   - ").append(filter.getClass().getSimpleName());
            }
            output.append("\n");
        }
        log.info(output.toString());
    }
}
