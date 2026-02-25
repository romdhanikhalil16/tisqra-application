package com.tisqra.event.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Event Schedule Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleDTO {
    private UUID id;
    private LocalTime time;
    private String title;
    private String description;
    private String speaker;
    private String location;
    private Integer sortOrder;
}
