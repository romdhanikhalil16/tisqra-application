package com.tisqra.analytics.application.service;

import com.tisqra.analytics.application.dto.DailySalesDTO;
import com.tisqra.analytics.application.dto.DashboardDTO;
import com.tisqra.analytics.application.dto.EventSalesDTO;
import com.tisqra.analytics.domain.model.AnalyticsEvent;
import com.tisqra.analytics.domain.model.SalesAnalytics;
import com.tisqra.analytics.domain.repository.AnalyticsEventRepository;
import com.tisqra.analytics.domain.repository.SalesAnalyticsRepository;
import com.tisqra.kafka.events.SalesRecordedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SalesAnalyticsRepository salesAnalyticsRepository;
    private final AnalyticsEventRepository analyticsEventRepository;

    @Transactional
    @KafkaListener(topics = "analytics.sales.recorded", groupId = "analytics-service")
    public void handleSalesRecordedEvent(SalesRecordedEvent event) {
        log.info("Recording sales analytics for order: {}", event.getOrderId());

        LocalDateTime soldAt = event.getSoldAt();
        LocalDate date = soldAt.toLocalDate();
        Integer hour = soldAt.getHour();

        // Find or create analytics record
        SalesAnalytics analytics = salesAnalyticsRepository
            .findByOrganizationIdAndEventIdAndDateAndHour(
                event.getOrganizationId(), 
                event.getEventId(), 
                date, 
                hour)
            .orElse(SalesAnalytics.builder()
                .organizationId(event.getOrganizationId())
                .eventId(event.getEventId())
                .date(date)
                .hour(hour)
                .currency(event.getCurrency())
                .build());

        // Calculate commission
        BigDecimal commission = event.getRevenue().subtract(event.getNetRevenue());

        // Add sale
        analytics.addSale(event.getTicketsSold(), event.getRevenue(), commission);
        salesAnalyticsRepository.save(analytics);

        // Track analytics event
        trackEvent("SALE_COMPLETED", event.getOrderId(), event.getOrganizationId(), 
            Map.of(
                "eventId", event.getEventId().toString(),
                "revenue", event.getRevenue().toString(),
                "ticketsSold", event.getTicketsSold()
            ));

        log.info("Sales analytics recorded successfully");
    }

    @Cacheable(value = "dashboards", key = "#organizationId + '-' + #startDate + '-' + #endDate")
    public DashboardDTO getDashboard(UUID organizationId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating dashboard for organization: {} from {} to {}", organizationId, startDate, endDate);

        // Get total metrics
        BigDecimal totalRevenue = salesAnalyticsRepository.getTotalRevenue(organizationId, startDate, endDate);
        Long totalTickets = salesAnalyticsRepository.getTotalTicketsSold(organizationId, startDate, endDate);

        // Get sales by event
        List<SalesAnalytics> eventSales = salesAnalyticsRepository
            .getTopEventsByRevenue(organizationId, startDate, endDate);

        // Group by event
        Map<UUID, EventSalesDTO> eventMap = new HashMap<>();
        for (SalesAnalytics sale : eventSales) {
            eventMap.computeIfAbsent(sale.getEventId(), k -> EventSalesDTO.builder()
                .eventId(sale.getEventId())
                .eventName("Event " + sale.getEventId()) // Would fetch from event service
                .ticketsSold(0)
                .revenue(BigDecimal.ZERO)
                .netRevenue(BigDecimal.ZERO)
                .build());

            EventSalesDTO eventDto = eventMap.get(sale.getEventId());
            eventDto.setTicketsSold(eventDto.getTicketsSold() + sale.getTicketsSold());
            eventDto.setRevenue(eventDto.getRevenue().add(sale.getRevenue()));
            eventDto.setNetRevenue(eventDto.getNetRevenue().add(sale.getNetRevenue()));
        }

        List<EventSalesDTO> topEvents = eventMap.values().stream()
            .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
            .limit(10)
            .collect(Collectors.toList());

        // Get daily sales
        List<SalesAnalytics> dailyData = salesAnalyticsRepository
            .findByOrganizationIdAndDateBetween(organizationId, startDate, endDate);

        Map<LocalDate, DailySalesDTO> dailyMap = new HashMap<>();
        for (SalesAnalytics sale : dailyData) {
            dailyMap.computeIfAbsent(sale.getDate(), k -> DailySalesDTO.builder()
                .date(sale.getDate())
                .ticketsSold(0)
                .revenue(BigDecimal.ZERO)
                .orders(0)
                .build());

            DailySalesDTO dailyDto = dailyMap.get(sale.getDate());
            dailyDto.setTicketsSold(dailyDto.getTicketsSold() + sale.getTicketsSold());
            dailyDto.setRevenue(dailyDto.getRevenue().add(sale.getRevenue()));
            dailyDto.setOrders(dailyDto.getOrders() + 1);
        }

        List<DailySalesDTO> dailySales = dailyMap.values().stream()
            .sorted(Comparator.comparing(DailySalesDTO::getDate))
            .collect(Collectors.toList());

        // Calculate net revenue
        BigDecimal netRevenue = eventSales.stream()
            .map(SalesAnalytics::getNetRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
            .startDate(startDate)
            .endDate(endDate)
            .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
            .netRevenue(netRevenue)
            .totalTicketsSold(totalTickets != null ? totalTickets : 0L)
            .totalOrders(dailyData.size())
            .topEvents(topEvents)
            .dailySales(dailySales)
            .demographics(Map.of()) // Would be populated from user data
            .build();
    }

    public List<EventSalesDTO> getEventSalesReport(UUID eventId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating event sales report for: {}", eventId);

        List<SalesAnalytics> sales = salesAnalyticsRepository
            .findByEventIdAndDateBetween(eventId, startDate, endDate);

        return sales.stream()
            .map(s -> EventSalesDTO.builder()
                .eventId(s.getEventId())
                .ticketsSold(s.getTicketsSold())
                .revenue(s.getRevenue())
                .netRevenue(s.getNetRevenue())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void trackEvent(String eventType, UUID aggregateId, UUID organizationId, Map<String, Object> data) {
        AnalyticsEvent event = AnalyticsEvent.builder()
            .eventType(eventType)
            .aggregateId(aggregateId)
            .organizationId(organizationId)
            .data(data)
            .occurredAt(LocalDateTime.now())
            .build();

        analyticsEventRepository.save(event);
    }
}
