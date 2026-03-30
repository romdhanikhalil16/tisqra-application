package com.tisqra.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Common fields for Kafka events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseKafkaEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String source;
    private Integer version;
}

