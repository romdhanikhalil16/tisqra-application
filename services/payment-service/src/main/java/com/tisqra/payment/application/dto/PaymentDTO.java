package com.tisqra.payment.application.dto;

import com.tisqra.common.enums.PaymentMethod;
import com.tisqra.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private String provider;
    private String providerPaymentId;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
