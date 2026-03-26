package com.tisqra.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dashboard Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private BigDecimal netRevenue;
    private Long totalTicketsSold;
    private Integer totalOrders;
    private List<EventSalesDTO> topEvents;
    private List<DailySalesDTO> dailySales;
    private Map<String, Object> demographics;
}
