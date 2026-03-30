package com.tisqra.organization.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.organization.application.dto.CreateOrganizationRequest;
import com.tisqra.organization.application.dto.OrganizationDTO;
import com.tisqra.organization.application.dto.UpdateOrganizationRequest;
import com.tisqra.organization.application.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganizationDTO>> createOrganization(
        @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDTO dto = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<OrganizationDTO>builder().success(true).data(dto).build());
    }

    @GetMapping
    @Operation(summary = "Get all organizations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrganizationDTO>>> getAllOrganizations(Pageable pageable) {
        Page<OrganizationDTO> organizations = organizationService.getAllOrganizations(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrganizationDTO>>builder().success(true).data(organizations).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganizationDTO>> getOrganizationById(@PathVariable UUID id) {
        OrganizationDTO dto = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.<OrganizationDTO>builder().success(true).data(dto).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganizationDTO>> updateOrganization(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationDTO dto = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.<OrganizationDTO>builder().success(true).data(dto).build());
    }
}

