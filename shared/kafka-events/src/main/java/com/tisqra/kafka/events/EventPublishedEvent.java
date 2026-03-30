package com.tisqra.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPublishedEvent {
    private UUID eventId;
    private UUID organizationId;
    private String eventName;
    private String slug;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime publishedAt;
}

