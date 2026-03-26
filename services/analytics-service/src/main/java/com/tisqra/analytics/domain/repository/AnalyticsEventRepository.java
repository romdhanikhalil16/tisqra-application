package com.tisqra.analytics.domain.repository;

import com.tisqra.analytics.domain.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Analytics Event repository interface
 */
@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    List<AnalyticsEvent> findByEventTypeAndOccurredAtBetween(
        String eventType, LocalDateTime start, LocalDateTime end);

    List<AnalyticsEvent> findByOrganizationIdAndOccurredAtBetween(
        UUID organizationId, LocalDateTime start, LocalDateTime end);
}
