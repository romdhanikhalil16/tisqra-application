package com.tisqra.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Emitted when an order is successfully paid.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String source;
    private Integer version;

    private UUID orderId;
    private UUID paymentId;
    private UUID userId;
    private UUID eventAggregateId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}

