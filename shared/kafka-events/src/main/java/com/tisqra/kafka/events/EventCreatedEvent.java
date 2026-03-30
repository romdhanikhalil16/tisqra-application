package com.tisqra.kafka.events;

import com.tisqra.common.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Emitted when a new event is created (before publish).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreatedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String source;
    private Integer version;

    private UUID organizationId;
    private String eventName;
    private String slug;
    private EventStatus status;
}

