package com.tisqra.ticket.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Local-dev safety valve:
 * If a developer accidentally edited already-applied Flyway migrations, Flyway validation will fail and
 * the service won't start. For the docker profile only, we optionally run Flyway repair before migrate.
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

