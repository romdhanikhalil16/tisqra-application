package com.tisqra.organization.infrastructure.controller;

import com.tisqra.organization.application.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Organization subscription APIs")
@SecurityRequirement(name = "bearer-jwt")
public class SubscriptionController {

    private final OrganizationService organizationService;

    @GetMapping("/organization/{organizationId}/can-create-event")
    @Operation(summary = "Check if an organization can create more events")
    @PreAuthorize("isAuthenticated()")
    public boolean canCreateEvent(@PathVariable UUID organizationId) {
        return organizationService.canCreateEvent(organizationId);
    }

    @PostMapping("/organization/{organizationId}/increment-event-count")
    @Operation(summary = "Increment organization event count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> incrementEventCount(@PathVariable UUID organizationId) {
        organizationService.incrementEventCount(organizationId);
        return ResponseEntity.ok().build();
    }
}

