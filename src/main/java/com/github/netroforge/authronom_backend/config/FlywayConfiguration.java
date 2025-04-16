package com.github.netroforge.authronom_backend.config;

import com.github.netroforge.authronom_backend.db.migrations.PrimaryBaseJavaMigration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class FlywayConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary.flyway")
    public FlywayProperties primaryFlywayProperties() {
        return new FlywayProperties();
    }

    @Bean
    public Flyway primaryFlyway(
            DataSource primaryDataSource,
            FlywayProperties primaryFlywayProperties,
            List<Callback> primaryCallbacks,
            List<PrimaryBaseJavaMigration> primaryMigrations
    ) {
        return createFlyway(primaryDataSource, primaryFlywayProperties, primaryCallbacks, primaryMigrations);
    }

    @Bean
    public FlywayMigrationInitializer primaryFlywayMigrationInitializer(
            Flyway primaryFlyway,
            ObjectProvider<FlywayMigrationStrategy> primaryFlywayMigrationStrategy
    ) {
        return new FlywayMigrationInitializer(
                primaryFlyway,
                primaryFlywayMigrationStrategy.getIfAvailable()
        );
    }

    private Flyway createFlyway(
            DataSource dataSource,
            FlywayProperties flywayProperties,
            List<? extends Callback> callbacks,
            List<? extends JavaMigration> migrations
    ) {
        FluentConfiguration fluentConfiguration = Flyway.configure();
        fluentConfiguration.dataSource(dataSource);
        fluentConfiguration.locations(flywayProperties.getLocations().toArray(new String[0]));
        fluentConfiguration.baselineOnMigrate(flywayProperties.isBaselineOnMigrate());
        fluentConfiguration.schemas(flywayProperties.getSchemas().toArray(new String[0]));
        fluentConfiguration.defaultSchema(flywayProperties.getDefaultSchema());
        fluentConfiguration.table(flywayProperties.getTable());
        fluentConfiguration.callbacks(callbacks.toArray(new Callback[0]));
        fluentConfiguration.javaMigrations(migrations.toArray(new JavaMigration[0]));
        return fluentConfiguration.load();
    }
}
