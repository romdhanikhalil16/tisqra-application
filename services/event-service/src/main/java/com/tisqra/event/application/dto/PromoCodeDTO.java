package com.tisqra.event.application.dto;

import com.tisqra.event.domain.model.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Promo Code Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {
    private UUID id;
    private String code;
    private UUID eventId;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private Boolean isValid;
}
