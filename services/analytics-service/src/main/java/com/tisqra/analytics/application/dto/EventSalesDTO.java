package com.tisqra.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event Sales Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSalesDTO {
    private UUID eventId;
    private String eventName;
    private Integer ticketsSold;
    private BigDecimal revenue;
    private BigDecimal netRevenue;
}
