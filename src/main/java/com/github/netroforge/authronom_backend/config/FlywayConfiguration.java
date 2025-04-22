package com.github.netroforge.authronom_backend.config;

import com.github.netroforge.authronom_backend.db.migration.primary.PrimaryBaseJavaMigration;
import com.github.netroforge.authronom_backend.db.migration.primary.PrimaryBaselineJavaMigration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.migration.baseline.BaselineJavaMigration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
            Optional<PrimaryBaselineJavaMigration> primaryBaselineMigrationOptional,
            List<PrimaryBaseJavaMigration> primaryMigrations
    ) {
        return createFlyway(
                primaryDataSource,
                primaryFlywayProperties,
                primaryCallbacks,
                primaryBaselineMigrationOptional,
                primaryMigrations
        );
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
            Optional<? extends BaselineJavaMigration> baselineMigrationOptional,
            List<? extends BaseJavaMigration> migrations
    ) {
        FluentConfiguration fluentConfiguration = Flyway.configure();
        fluentConfiguration.dataSource(dataSource);
        fluentConfiguration.locations(flywayProperties.getLocations().toArray(new String[0]));
        fluentConfiguration.baselineOnMigrate(flywayProperties.isBaselineOnMigrate());
        fluentConfiguration.schemas(flywayProperties.getSchemas().toArray(new String[0]));
        fluentConfiguration.defaultSchema(flywayProperties.getDefaultSchema());
        fluentConfiguration.table(flywayProperties.getTable());
        fluentConfiguration.callbacks(callbacks.toArray(new Callback[0]));
        if (baselineMigrationOptional.isPresent()) {
            fluentConfiguration.javaMigrations(
                    Stream.concat(
                            Stream.of(baselineMigrationOptional.get()),
                            migrations.stream()
                    ).toArray(JavaMigration[]::new)
            );
        } else {
            fluentConfiguration.javaMigrations(
                    migrations.toArray(JavaMigration[]::new)
            );
        }
        return fluentConfiguration.load();
    }
}
