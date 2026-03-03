package com.tisqra.ticket.domain.model;

import com.tisqra.common.enums.TicketStatus;
import com.tisqra.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ticket domain entity
 * Represents a ticket with QR code for event entry
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_number", columnList = "ticketNumber"),
    @Index(name = "idx_ticket_qr_code", columnList = "qrCode"),
    @Index(name = "idx_ticket_order_id", columnList = "orderId"),
    @Index(name = "idx_ticket_event_id", columnList = "eventId"),
    @Index(name = "idx_ticket_owner_email", columnList = "ownerEmail")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID ticketCategoryId;

    @Column(nullable = false, length = 150)
    private String ticketCategoryName;

    @Column(nullable = false, unique = true, length = 255)
    private String qrCode;

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] qrCodeImage;

    @Column(nullable = false, length = 255)
    private String ownerEmail;

    @Column(nullable = false, length = 150)
    private String ownerName;

    @Embedded
    private AttendeeInfo attendee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isTransferable = true;

    @Column
    private LocalDateTime validatedAt;

    @Column
    private UUID validatedBy;

    @Column(length = 100)
    private String validatedByName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business methods
    public void validate(UUID scannerId, String scannerName) {
        if (status != TicketStatus.ACTIVE) {
            throw new BusinessException("Ticket is not active and cannot be validated");
        }
        if (validatedAt != null) {
            throw new BusinessException("Ticket has already been validated");
        }
        this.status = TicketStatus.VALIDATED;
        this.validatedAt = LocalDateTime.now();
        this.validatedBy = scannerId;
        this.validatedByName = scannerName;
    }

    public void transfer(String newOwnerEmail, String newOwnerName) {
        if (!isTransferable) {
            throw new BusinessException("This ticket is not transferable");
        }
        if (status == TicketStatus.VALIDATED) {
            throw new BusinessException("Cannot transfer validated ticket");
        }
        if (status != TicketStatus.ACTIVE) {
            throw new BusinessException("Only active tickets can be transferred");
        }
        this.ownerEmail = newOwnerEmail;
        this.ownerName = newOwnerName;
        this.status = TicketStatus.TRANSFERRED;
    }

    public void cancel() {
        if (status == TicketStatus.VALIDATED) {
            throw new BusinessException("Cannot cancel validated ticket");
        }
        this.status = TicketStatus.CANCELLED;
    }

    public void refund() {
        if (status == TicketStatus.VALIDATED) {
            throw new BusinessException("Cannot refund validated ticket");
        }
        this.status = TicketStatus.REFUNDED;
    }

    public void invalidate() {
        this.status = TicketStatus.CANCELLED;
    }
}
