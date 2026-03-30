package com.tisqra.event.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.common.enums.EventCategory;
import com.tisqra.event.application.dto.CreateEventRequest;
import com.tisqra.event.application.dto.EventDTO;
import com.tisqra.event.application.service.EventService;
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

/**
 * Event REST Controller
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create a new event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventDTO event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<EventDTO>builder().success(true).data(event).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable UUID id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.<EventDTO>builder().success(true).data(event).build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get event by slug")
    public ResponseEntity<ApiResponse<EventDTO>> getEventBySlug(@PathVariable String slug) {
        EventDTO event = eventService.getEventBySlug(slug);
        return ResponseEntity.ok(ApiResponse.<EventDTO>builder().success(true).data(event).build());
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get events by organization")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> getEventsByOrganization(
            @PathVariable UUID organizationId,
            Pageable pageable) {
        Page<EventDTO> events = eventService.getEventsByOrganization(organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<EventDTO>>builder().success(true).data(events).build());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming published events")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> getUpcomingEvents(Pageable pageable) {
        Page<EventDTO> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<EventDTO>>builder().success(true).data(events).build());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get events by category")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> getEventsByCategory(
            @PathVariable EventCategory category,
            Pageable pageable) {
        Page<EventDTO> events = eventService.getEventsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<EventDTO>>builder().success(true).data(events).build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search events")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> searchEvents(
            @RequestParam String query,
            Pageable pageable) {
        Page<EventDTO> events = eventService.searchEvents(query, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<EventDTO>>builder().success(true).data(events).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEventRequest request) {
        EventDTO event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.<EventDTO>builder().success(true).data(event).build());
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<EventDTO>> publishEvent(@PathVariable UUID id) {
        EventDTO event = eventService.publishEvent(id);
        return ResponseEntity.ok(ApiResponse.<EventDTO>builder().success(true).data(event).build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(@PathVariable UUID id) {
        eventService.cancelEvent(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}
