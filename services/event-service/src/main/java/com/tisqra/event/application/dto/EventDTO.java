package com.tisqra.event.application.dto;

import com.tisqra.common.enums.EventCategory;
import com.tisqra.common.enums.EventStatus;
import com.tisqra.event.domain.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String slug;
    private String description;
    private EventCategory category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Location location;
    private Integer capacity;
    private EventStatus status;
    private String bannerImageUrl;
    private String thumbnailImageUrl;
    private List<TicketCategoryDTO> categories;
    private List<EventScheduleDTO> schedule;
    private Integer availableTickets;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
