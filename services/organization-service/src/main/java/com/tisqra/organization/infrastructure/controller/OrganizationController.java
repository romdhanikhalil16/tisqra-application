package com.tisqra.organization.infrastructure.controller;

import com.tisqra.organization.application.dto.CreateOrganizationRequest;
import com.tisqra.organization.application.dto.OrganizationDTO;
import com.tisqra.organization.application.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Organization REST Controller
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<OrganizationDTO> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDTO organization = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable UUID id) {
        OrganizationDTO organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get organization by slug")
    public ResponseEntity<OrganizationDTO> getOrganizationBySlug(@PathVariable String slug) {
        OrganizationDTO organization = organizationService.getOrganizationBySlug(slug);
        return ResponseEntity.ok(organization);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get organizations by owner")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<List<OrganizationDTO>> getOrganizationsByOwner(@PathVariable UUID ownerId) {
        List<OrganizationDTO> organizations = organizationService.getOrganizationsByOwner(ownerId);
        return ResponseEntity.ok(organizations);
    }

    @GetMapping
    @Operation(summary = "Get all active organizations")
    public ResponseEntity<List<OrganizationDTO>> getAllActiveOrganizations() {
        List<OrganizationDTO> organizations = organizationService.getAllActiveOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDTO organization = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(organization);
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify organization")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> verifyOrganization(@PathVariable UUID id) {
        organizationService.verifyOrganization(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate organization")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateOrganization(@PathVariable UUID id) {
        organizationService.deactivateOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
