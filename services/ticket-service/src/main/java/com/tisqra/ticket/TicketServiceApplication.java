package com.tisqra.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Ticket Service Application
 * Manages ticket generation, QR codes, validation, and transfers
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}
