package com.tisqra.analytics.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sales Analytics domain entity
 * Aggregates sales data for reporting
 */
@Entity
@Table(name = "sales_analytics", indexes = {
    @Index(name = "idx_sales_org_id", columnList = "organizationId"),
    @Index(name = "idx_sales_event_id", columnList = "eventId"),
    @Index(name = "idx_sales_date", columnList = "date"),
    @Index(name = "idx_sales_date_hour", columnList = "date, hour")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID organizationId;

    @Column
    private UUID eventId;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private Integer hour;

    @Column(nullable = false)
    @Builder.Default
    private Integer ticketsSold = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal commissionPercentage = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Business methods
    public void addSale(Integer tickets, BigDecimal amount, BigDecimal commission) {
        this.ticketsSold += tickets;
        this.revenue = this.revenue.add(amount);
        this.netRevenue = this.netRevenue.add(amount.subtract(commission));
    }
}
