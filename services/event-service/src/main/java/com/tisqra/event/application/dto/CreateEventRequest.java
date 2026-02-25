package com.tisqra.event.application.dto;

import com.tisqra.common.enums.EventCategory;
import com.tisqra.event.domain.model.Location;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating an event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotBlank(message = "Event name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private EventCategory category;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Location is required")
    private Location location;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private String bannerImageUrl;

    private String thumbnailImageUrl;

    private List<CreateTicketCategoryRequest> ticketCategories;

    private List<CreateEventScheduleRequest> scheduleItems;
}
