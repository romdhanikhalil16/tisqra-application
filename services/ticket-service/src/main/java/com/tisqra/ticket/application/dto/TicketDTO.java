package com.tisqra.ticket.application.dto;

import com.tisqra.common.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private String qrCode;
    private String ownerEmail;
    private String ownerName;
    private UUID ownerUserId;
    private TicketStatus status;
    private Boolean isTransferable;
    private LocalDateTime validatedAt;
    private UUID validatedBy;
    private String scannerDeviceId;
}

