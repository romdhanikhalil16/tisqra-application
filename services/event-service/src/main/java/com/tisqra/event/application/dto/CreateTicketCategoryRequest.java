package com.tisqra.event.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a ticket category
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private LocalDateTime saleStartDate;

    private LocalDateTime saleEndDate;

    private String color;

    private List<String> features;
}
