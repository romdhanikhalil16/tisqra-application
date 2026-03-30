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
public class TicketValidatedEvent {
    private UUID ticketId;
    private String ticketNumber;
    private UUID eventId;
    private UUID validatedBy;
    private LocalDateTime validatedAt;
    private String scannerDeviceId;
}

