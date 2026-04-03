package com.tisqra.organization.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Local-dev safety valve:
 * Allows the docker profile to self-heal if a developer edited already-applied Flyway migrations
 * or if a previous migration attempt left a failed entry in flyway_schema_history.
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

