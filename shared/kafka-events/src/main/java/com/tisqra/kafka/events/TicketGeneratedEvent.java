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
public class TicketGeneratedEvent {
    private UUID ticketId;
    private String ticketNumber;
    private UUID orderId;
    private UUID eventId;
    private UUID userId;
    private String ownerEmail;
    private String ownerName;
    private String qrCode;
    private LocalDateTime generatedAt;
}

