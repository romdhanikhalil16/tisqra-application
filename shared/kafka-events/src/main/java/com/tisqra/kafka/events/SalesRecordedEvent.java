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
public class SalesRecordedEvent {
    private UUID orderId;
    private UUID eventId;
    private UUID organizationId;
    private BigDecimal revenue;
    private BigDecimal netRevenue;
    private Integer ticketsSold;
    private String currency;
    private LocalDateTime soldAt;
}

