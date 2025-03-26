package com.github.netroforge.authronom_backend.config;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.UIServiceEnum;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Configuration
public class LiquibaseConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary.liquibase")
    public LiquibaseProperties primaryLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase primaryLiquibase(
            HikariDataSource primaryDataSource,
            LiquibaseProperties primaryLiquibaseProperties
    ) {
        return springLiquibase(primaryDataSource, primaryLiquibaseProperties);
    }

    /**
     * Inspired by LiquibaseAutoConfiguration#liquibase
     */
    private SpringLiquibase springLiquibase(HikariDataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setClearCheckSums(properties.isClearChecksums());
        if (!CollectionUtils.isEmpty(properties.getContexts())) {
            liquibase.setContexts(StringUtils.collectionToCommaDelimitedString(properties.getContexts()));
        }
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        if (!CollectionUtils.isEmpty(properties.getLabelFilter())) {
            liquibase.setLabelFilter(StringUtils.collectionToCommaDelimitedString(properties.getLabelFilter()));
        }
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
        liquibase.setTag(properties.getTag());
        if (properties.getShowSummary() != null) {
            liquibase.setShowSummary(UpdateSummaryEnum.valueOf(properties.getShowSummary().name()));
        }
        if (properties.getShowSummaryOutput() != null) {
            liquibase
                    .setShowSummaryOutput(UpdateSummaryOutputEnum.valueOf(properties.getShowSummaryOutput().name()));
        }
        if (properties.getUiService() != null) {
            liquibase.setUiService(UIServiceEnum.valueOf(properties.getUiService().name()));
        }
        return liquibase;
    }
}
