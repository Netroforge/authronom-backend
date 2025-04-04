package com.github.netroforge.authronom_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Slf4j
@Configuration
public class SecurityFilterLoggerConfig {

    @Bean
    public String logSecurityFilters(FilterChainProxy filterChainProxy) {
        StringBuilder output = new StringBuilder();
        output.append("Spring Security Filter Chains:");
        List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();
        for (int i = 0; i < filterChains.size(); i++) {
            SecurityFilterChain chain = filterChains.get(i);
            List<String> filters = chain
                    .getFilters()
                    .stream()
                    .map(filter -> filter.getClass().getSimpleName())
                    .toList();

            output.append("\n Filter Chain #").append(i + 1);
            output.append("\n  Request Matcher: ").append(chain); // Prints the matcher details
            output.append("\n  Filters:");
            filters.forEach(filter -> output.append("\n   - ").append(filter));
        }
        log.info(output.toString());
        return "test";
    }
}
