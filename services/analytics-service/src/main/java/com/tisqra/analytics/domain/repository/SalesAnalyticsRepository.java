package com.tisqra.analytics.domain.repository;

import com.tisqra.analytics.domain.model.SalesAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Sales Analytics repository interface
 */
@Repository
public interface SalesAnalyticsRepository extends JpaRepository<SalesAnalytics, UUID> {

    Optional<SalesAnalytics> findByOrganizationIdAndEventIdAndDateAndHour(
        UUID organizationId, UUID eventId, LocalDate date, Integer hour);

    List<SalesAnalytics> findByOrganizationIdAndDateBetween(
        UUID organizationId, LocalDate startDate, LocalDate endDate);

    List<SalesAnalytics> findByEventIdAndDateBetween(
        UUID eventId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.revenue) FROM SalesAnalytics s WHERE s.organizationId = :orgId AND s.date BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(
        @Param("orgId") UUID organizationId, 
        @Param("start") LocalDate startDate, 
        @Param("end") LocalDate endDate);

    @Query("SELECT SUM(s.ticketsSold) FROM SalesAnalytics s WHERE s.organizationId = :orgId AND s.date BETWEEN :start AND :end")
    Long getTotalTicketsSold(
        @Param("orgId") UUID organizationId, 
        @Param("start") LocalDate startDate, 
        @Param("end") LocalDate endDate);

    @Query("SELECT s FROM SalesAnalytics s WHERE s.organizationId = :orgId AND s.date BETWEEN :start AND :end ORDER BY s.revenue DESC")
    List<SalesAnalytics> getTopEventsByRevenue(
        @Param("orgId") UUID organizationId, 
        @Param("start") LocalDate startDate, 
        @Param("end") LocalDate endDate);
}
