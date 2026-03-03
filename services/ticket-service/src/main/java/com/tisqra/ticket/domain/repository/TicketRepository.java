package com.tisqra.ticket.domain.repository;

import com.tisqra.common.enums.TicketStatus;
import com.tisqra.ticket.domain.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ticket repository interface
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByQrCode(String qrCode);

    List<Ticket> findByOrderId(UUID orderId);

    Page<Ticket> findByOwnerEmail(String ownerEmail, Pageable pageable);

    List<Ticket> findByEventId(UUID eventId);

    List<Ticket> findByEventIdAndStatus(UUID eventId, TicketStatus status);

    Long countByEventIdAndStatus(UUID eventId, TicketStatus status);

    boolean existsByQrCode(String qrCode);
}
