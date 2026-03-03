package com.tisqra.ticket.domain.repository;

import com.tisqra.ticket.domain.model.TicketTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Ticket Transfer repository interface
 */
@Repository
public interface TicketTransferRepository extends JpaRepository<TicketTransfer, UUID> {

    List<TicketTransfer> findByTicketId(UUID ticketId);

    List<TicketTransfer> findByToEmailAndAcceptedFalse(String toEmail);
}
