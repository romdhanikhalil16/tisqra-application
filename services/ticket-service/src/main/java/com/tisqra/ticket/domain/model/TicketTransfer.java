package com.tisqra.ticket.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ticket Transfer domain entity
 * Tracks ticket ownership transfers
 */
@Entity
@Table(name = "ticket_transfers", indexes = {
    @Index(name = "idx_transfer_ticket_id", columnList = "ticketId"),
    @Index(name = "idx_transfer_to_email", columnList = "toEmail")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ticketId;

    @Column(nullable = false, length = 255)
    private String fromEmail;

    @Column(nullable = false, length = 255)
    private String toEmail;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Boolean accepted = false;

    @Column
    private LocalDateTime acceptedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void accept() {
        this.accepted = true;
        this.acceptedAt = LocalDateTime.now();
    }
}
