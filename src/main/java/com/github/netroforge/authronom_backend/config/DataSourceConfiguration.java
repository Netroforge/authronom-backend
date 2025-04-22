package com.github.netroforge.authronom_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@EnableJdbcRepositories(value = "com.github.netroforge.authronom_backend.db.repository")
@Configuration
public class DataSourceConfiguration extends AbstractJdbcConfiguration {
}
