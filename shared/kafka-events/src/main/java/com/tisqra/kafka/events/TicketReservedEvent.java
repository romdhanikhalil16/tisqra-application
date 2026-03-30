package com.tisqra.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Emitted when tickets are reserved for an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String source;
    private Integer version;

    private UUID orderId;
    private UUID ticketCategoryId;
    private Integer quantity;
    private UUID userId;
}

