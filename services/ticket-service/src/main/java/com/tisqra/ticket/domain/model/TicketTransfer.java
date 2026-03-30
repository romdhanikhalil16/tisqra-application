package com.tisqra.ticket.domain.model;

import com.tisqra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_transfers")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTransfer extends BaseEntity {

    @Column(nullable = false)
    private UUID ticketId;

    @Column(length = 200)
    private String fromEmail;

    @Column(length = 200)
    private String toEmail;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Boolean accepted = true;

    private LocalDateTime acceptedAt;
}

