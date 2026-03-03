package com.tisqra.ticket.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Validation result value object
 * Result of ticket QR code validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private Boolean valid;
    private String reason;
    private String ticketNumber;
    private String eventName;
    private String ownerName;
    private String categoryName;
    private String message;
    private LocalDateTime validatedAt;
    private String validatedBy;
}
