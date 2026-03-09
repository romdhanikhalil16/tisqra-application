package com.tisqra.organization.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Subscription plan domain entity
 * Defines pricing tiers and feature limits
 */
@Entity
@Table(name = "subscription_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceYearly;

    @Column(nullable = false)
    private Integer maxEventsPerMonth;

    @Column(nullable = false)
    private Integer maxTicketsPerEvent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> features;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer sortOrder;

    // Business methods
    public boolean isFeatureEnabled(String featureName) {
        if (features == null) {
            return false;
        }
        Object feature = features.get(featureName);
        return feature instanceof Boolean && (Boolean) feature;
    }

    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount.multiply(commissionPercentage).divide(new BigDecimal("100"));
    }
}
