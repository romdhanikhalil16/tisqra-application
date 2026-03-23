package com.tisqra.order.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotEmpty(message = "Order items are required")
    private List<CreateOrderItemRequest> items;

    private String promoCode;
}
