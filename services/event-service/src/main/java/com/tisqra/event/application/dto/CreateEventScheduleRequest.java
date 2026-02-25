package com.tisqra.event.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Request DTO for creating event schedule item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventScheduleRequest {

    @NotNull(message = "Time is required")
    private LocalTime time;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String speaker;

    private String location;

    private Integer sortOrder;
}
