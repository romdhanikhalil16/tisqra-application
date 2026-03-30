package com.tisqra.ticket.domain.repository;

import com.tisqra.ticket.domain.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByQrCode(String qrCode);

    Page<Ticket> findByOrderId(UUID orderId, Pageable pageable);

    Page<Ticket> findByOwnerUserId(UUID ownerUserId, Pageable pageable);
}

