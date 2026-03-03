package com.tisqra.ticket.application.dto;

import com.tisqra.common.enums.TicketStatus;
import com.tisqra.ticket.domain.model.AttendeeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ticket Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private UUID id;
    private String ticketNumber;
    private UUID orderId;
    private UUID eventId;
    private UUID ticketCategoryId;
    private String ticketCategoryName;
    private String qrCode;
    private String qrCodeImageBase64;
    private String ownerEmail;
    private String ownerName;
    private AttendeeInfo attendee;
    private TicketStatus status;
    private Boolean isTransferable;
    private LocalDateTime validatedAt;
    private String validatedByName;
    private LocalDateTime createdAt;
}
