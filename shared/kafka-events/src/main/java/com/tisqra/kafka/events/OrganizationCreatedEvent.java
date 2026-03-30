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
public class OrganizationCreatedEvent {
    private UUID organizationId;
    private String name;
    private UUID ownerId;
    private String subscriptionPlan;
    private LocalDateTime createdAt;
    private String eventId;
}

