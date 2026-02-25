package com.tisqra.event.domain.model;

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
 * Promo Code domain entity
 * Provides discounts for event tickets
 */
@Entity
@Table(name = "promo_codes", indexes = {
    @Index(name = "idx_promo_code", columnList = "code"),
    @Index(name = "idx_promo_event_id", columnList = "eventId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column
    private Integer maxUses;

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(validFrom) && 
               now.isBefore(validUntil) &&
               (maxUses == null || usedCount < maxUses);
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isValid()) {
            throw new BusinessException("Promo code is not valid");
        }

        return switch (discountType) {
            case PERCENTAGE -> amount.multiply(discountValue).divide(new BigDecimal("100"));
            case FIXED_AMOUNT -> discountValue.min(amount);
        };
    }

    public void incrementUsage() {
        if (!isValid()) {
            throw new BusinessException("Promo code is not valid");
        }
        this.usedCount++;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
