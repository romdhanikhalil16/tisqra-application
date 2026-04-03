package com.tisqra.event.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Local-dev safety valve:
 * For docker profile only, optionally runs Flyway repair before migrate.
 * Helps recover from failed migrations/checksum edits in local volumes.
 *
 * IMPORTANT: Do not enable this in production environments.
 */
@Configuration
@Profile("docker")
public class FlywayRepairConfig {

    @Bean
    @ConditionalOnProperty(prefix = "tisqra.flyway", name = "repair-on-startup", havingValue = "true")
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}

