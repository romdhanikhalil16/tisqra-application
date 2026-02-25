package com.tisqra.event.domain.repository;

import com.tisqra.common.enums.EventCategory;
import com.tisqra.common.enums.EventStatus;
import com.tisqra.event.domain.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Event repository interface
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findBySlug(String slug);

    Page<Event> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByCategory(EventCategory category, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.startDate >= :now ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Event> searchEvents(@Param("query") String query, Pageable pageable);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizationId = :organizationId AND " +
           "e.createdAt >= :startOfMonth")
    Integer countByOrganizationIdAndCreatedAtAfter(
        @Param("organizationId") UUID organizationId, 
        @Param("startOfMonth") LocalDateTime startOfMonth
    );
}
