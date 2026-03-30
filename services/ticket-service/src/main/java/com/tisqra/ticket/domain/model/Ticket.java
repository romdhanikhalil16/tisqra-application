package com.tisqra.ticket.domain.model;

import com.tisqra.common.BaseEntity;
import com.tisqra.common.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_order_id", columnList = "orderId"),
    @Index(name = "idx_ticket_event_id", columnList = "eventId"),
    @Index(name = "idx_ticket_qr_code", columnList = "qrCode"),
    @Index(name = "idx_ticket_owner_user_id", columnList = "ownerUserId"),
    @Index(name = "idx_ticket_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String ticketNumber;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID ticketCategoryId;

    @Column(nullable = false, unique = true, length = 300)
    private String qrCode;

    @Lob
    @Column(name = "qr_code_image")
    private byte[] qrCodeImage;

    @Column(length = 200)
    private String ownerEmail;

    @Column(length = 200)
    private String ownerName;

    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isTransferable = true;

    private LocalDateTime validatedAt;

    private UUID validatedBy;

    private String scannerDeviceId;

    public void validate(UUID scannerId, String scannerDeviceId) {
        if (status != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Ticket is not in ACTIVE state");
        }
        this.status = TicketStatus.VALIDATED;
        this.validatedBy = scannerId;
        this.validatedAt = LocalDateTime.now();
        this.scannerDeviceId = scannerDeviceId;
    }

    public void transfer(String recipientEmail, String recipientName) {
        this.ownerEmail = recipientEmail;
        this.ownerName = recipientName;
        this.status = TicketStatus.TRANSFERRED;
        this.isTransferable = false; // after transfer, prevent further transfers by default
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
        this.isTransferable = false;
    }
}

