package com.tisqra.organization.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Subscription Plan Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private Integer maxEventsPerMonth;
    private Integer maxTicketsPerEvent;
    private BigDecimal commissionPercentage;
    private Map<String, Object> features;
    private Boolean isActive;
}
