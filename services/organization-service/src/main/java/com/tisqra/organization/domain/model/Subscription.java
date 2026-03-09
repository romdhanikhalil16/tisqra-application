package com.tisqra.organization.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subscription domain entity
 * Links organization to subscription plan
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_org_id", columnList = "organizationId"),
    @Index(name = "idx_subscription_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private LocalDateTime trialEndDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer eventsCreatedThisMonth = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }

    public boolean canCreateEvent() {
        return isActive() && eventsCreatedThisMonth < plan.getMaxEventsPerMonth();
    }

    public void incrementEventCount() {
        this.eventsCreatedThisMonth++;
    }

    public void resetMonthlyEventCount() {
        this.eventsCreatedThisMonth = 0;
    }

    public boolean isInTrial() {
        return status == SubscriptionStatus.TRIAL && 
               trialEndDate != null && 
               trialEndDate.isAfter(LocalDateTime.now());
    }

    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }
}
