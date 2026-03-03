package com.tisqra.ticket.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for generating tickets from an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTicketsRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;
}
