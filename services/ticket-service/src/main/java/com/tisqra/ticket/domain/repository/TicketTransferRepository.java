package com.tisqra.ticket.domain.repository;

import com.tisqra.ticket.domain.model.TicketTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TicketTransferRepository extends JpaRepository<TicketTransfer, UUID> {
}

