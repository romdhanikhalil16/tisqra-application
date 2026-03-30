package com.tisqra.analytics.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.analytics.application.dto.DashboardDTO;
import com.tisqra.analytics.application.dto.EventSalesDTO;
import com.tisqra.analytics.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Analytics REST Controller
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting APIs")
@SecurityRequirement(name = "bearer-jwt")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/{organizationId}")
    @Operation(summary = "Get organization dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard(
            @PathVariable UUID organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DashboardDTO dashboard = analyticsService.getDashboard(organizationId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.<DashboardDTO>builder().success(true).data(dashboard).build());
    }

    @GetMapping("/event/{eventId}/sales")
    @Operation(summary = "Get event sales report")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<List<EventSalesDTO>>> getEventSalesReport(
            @PathVariable UUID eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EventSalesDTO> report = analyticsService.getEventSalesReport(eventId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.<List<EventSalesDTO>>builder().success(true).data(report).build());
    }
}
