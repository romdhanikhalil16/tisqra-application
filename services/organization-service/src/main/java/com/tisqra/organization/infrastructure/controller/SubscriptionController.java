package com.tisqra.organization.infrastructure.controller;

import com.tisqra.organization.application.dto.SubscriptionDTO;
import com.tisqra.organization.application.dto.SubscriptionPlanDTO;
import com.tisqra.organization.application.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Subscription REST Controller
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get organization subscription")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<SubscriptionDTO> getOrganizationSubscription(@PathVariable UUID organizationId) {
        SubscriptionDTO subscription = subscriptionService.getOrganizationSubscription(organizationId);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/plans")
    @Operation(summary = "Get all subscription plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getAllPlans() {
        List<SubscriptionPlanDTO> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/organization/{organizationId}/upgrade")
    @Operation(summary = "Upgrade subscription plan")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<SubscriptionDTO> upgradeSubscription(
            @PathVariable UUID organizationId,
            @RequestParam String planCode) {
        SubscriptionDTO subscription = subscriptionService.upgradeSubscription(organizationId, planCode);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/organization/{organizationId}/cancel")
    @Operation(summary = "Cancel subscription")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID organizationId) {
        subscriptionService.cancelSubscription(organizationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/organization/{organizationId}/can-create-event")
    @Operation(summary = "Check if organization can create events")
    public ResponseEntity<Boolean> canCreateEvent(@PathVariable UUID organizationId) {
        boolean canCreate = subscriptionService.canOrganizationCreateEvent(organizationId);
        return ResponseEntity.ok(canCreate);
    }
}
