package com.tisqra.event.domain.repository;

import com.tisqra.event.domain.model.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Ticket Category repository interface
 */
@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    List<TicketCategory> findByEventId(UUID eventId);

    void deleteByEventId(UUID eventId);
}
