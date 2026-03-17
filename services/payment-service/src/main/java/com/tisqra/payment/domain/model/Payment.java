package com.tisqra.payment.domain.model;

import com.tisqra.common.enums.PaymentMethod;
import com.tisqra.common.enums.PaymentStatus;
import com.tisqra.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment domain entity
 * Represents a payment transaction
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "orderId"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_provider_id", columnList = "providerPaymentId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String provider = "MOCK_GATEWAY";

    @Column(length = 100)
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business methods
    public void complete(String providerPaymentId) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw new BusinessException("Only pending or processing payments can be completed");
        }
        this.status = PaymentStatus.COMPLETED;
        this.providerPaymentId = providerPaymentId;
        this.paidAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (status == PaymentStatus.COMPLETED) {
            throw new BusinessException("Cannot fail a completed payment");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void refund() {
        if (status != PaymentStatus.COMPLETED) {
            throw new BusinessException("Only completed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public void partiallyRefund() {
        if (status != PaymentStatus.COMPLETED && status != PaymentStatus.REFUNDED) {
            throw new BusinessException("Invalid payment status for partial refund");
        }
        this.status = PaymentStatus.PARTIALLY_REFUNDED;
    }

    public void process() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be processed");
        }
        this.status = PaymentStatus.PROCESSING;
    }
}
