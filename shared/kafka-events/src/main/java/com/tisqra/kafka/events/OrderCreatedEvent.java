package com.tisqra.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID userId;
    private UUID eventId;
    private BigDecimal totalAmount;
    private String currency;
    private Integer ticketCount;
    private LocalDateTime createdAt;
    private String kafkaEventId;
}

