package com.github.netroforge.authronom_backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

@EnableJdbcRepositories(value = "com.github.netroforge.authronom_backend.db.repository.primary")
@Configuration
public class DataSourceConfiguration extends AbstractJdbcConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.primary.hikari")
    public HikariDataSource primaryDataSource(DataSourceProperties primaryDataSourceProperties) {
        return primaryDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource primaryDataSource) {
        return new NamedParameterJdbcTemplate(primaryDataSource);
    }

    @Bean
    public TransactionManager transactionManager(DataSource primaryDataSource) {
        return new DataSourceTransactionManager(primaryDataSource);
    }
}
