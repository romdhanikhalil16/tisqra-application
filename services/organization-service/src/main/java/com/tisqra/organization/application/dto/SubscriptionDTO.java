package com.tisqra.organization.application.dto;

import com.tisqra.organization.domain.model.BillingCycle;
import com.tisqra.organization.domain.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subscription Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private UUID id;
    private UUID organizationId;
    private SubscriptionPlanDTO plan;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime trialEndDate;
    private Integer eventsCreatedThisMonth;
    private Boolean canCreateEvent;
}
