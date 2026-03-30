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
public class TicketTransferredEvent {
    private UUID ticketId;
    private String ticketNumber;
    private UUID eventId;
    private String fromEmail;
    private String toEmail;
    private LocalDateTime transferredAt;
}

