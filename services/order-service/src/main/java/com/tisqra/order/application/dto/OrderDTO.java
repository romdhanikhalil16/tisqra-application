package com.tisqra.order.application.dto;

import com.tisqra.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private UUID eventId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String promoCode;
    private List<OrderItemDTO> items;
    private Integer totalTickets;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}
