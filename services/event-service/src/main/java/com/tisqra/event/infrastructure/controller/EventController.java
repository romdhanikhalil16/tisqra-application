package com.tisqra.event.infrastructure.controller;

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
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventDTO event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<EventDTO> getEventById(@PathVariable UUID id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get event by slug")
    public ResponseEntity<EventDTO> getEventBySlug(@PathVariable String slug) {
        EventDTO event = eventService.getEventBySlug(slug);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get events by organization")
    public ResponseEntity<Page<EventDTO>> getEventsByOrganization(
            @PathVariable UUID organizationId,
            Pageable pageable) {
        Page<EventDTO> events = eventService.getEventsByOrganization(organizationId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming published events")
    public ResponseEntity<Page<EventDTO>> getUpcomingEvents(Pageable pageable) {
        Page<EventDTO> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get events by category")
    public ResponseEntity<Page<EventDTO>> getEventsByCategory(
            @PathVariable EventCategory category,
            Pageable pageable) {
        Page<EventDTO> events = eventService.getEventsByCategory(category, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    @Operation(summary = "Search events")
    public ResponseEntity<Page<EventDTO>> searchEvents(
            @RequestParam String query,
            Pageable pageable) {
        Page<EventDTO> events = eventService.searchEvents(query, pageable);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEventRequest request) {
        EventDTO event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<EventDTO> publishEvent(@PathVariable UUID id) {
        EventDTO event = eventService.publishEvent(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel event")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<Void> cancelEvent(@PathVariable UUID id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }
}
