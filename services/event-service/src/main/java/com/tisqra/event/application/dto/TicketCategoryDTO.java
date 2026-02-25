package com.tisqra.event.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Ticket Category Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategoryDTO {
    private UUID id;
    private UUID eventId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer quantity;
    private Integer soldCount;
    private Integer availableCount;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;
    private String color;
    private List<String> features;
    private Boolean isAvailable;
}
