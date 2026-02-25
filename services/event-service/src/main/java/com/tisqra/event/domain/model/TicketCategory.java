package com.tisqra.event.domain.model;

import com.tisqra.common.exception.BusinessException;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ticket Category domain entity
 * Represents a pricing tier for event tickets
 */
@Entity
@Table(name = "ticket_categories", indexes = {
    @Index(name = "idx_ticket_cat_event_id", columnList = "eventId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer soldCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedCount = 0;

    @Column
    private LocalDateTime saleStartDate;

    @Column
    private LocalDateTime saleEndDate;

    @Column(length = 7)
    private String color;

    @Type(ListArrayType.class)
    @Column(columnDefinition = "text[]")
    @Builder.Default
    private List<String> features = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        boolean withinSalePeriod = (saleStartDate == null || now.isAfter(saleStartDate)) &&
                                   (saleEndDate == null || now.isBefore(saleEndDate));
        return withinSalePeriod && getAvailableCount() > 0;
    }

    public Integer getAvailableCount() {
        return quantity - soldCount - reservedCount;
    }

    public void incrementSold(Integer count) {
        if (soldCount + count > quantity) {
            throw new BusinessException("Not enough tickets available");
        }
        this.soldCount += count;
    }

    public void reserve(Integer count) {
        if (getAvailableCount() < count) {
            throw new BusinessException("Not enough tickets available for reservation");
        }
        this.reservedCount += count;
    }

    public void releaseReservation(Integer count) {
        this.reservedCount = Math.max(0, this.reservedCount - count);
    }

    public void confirmReservation(Integer count) {
        if (reservedCount < count) {
            throw new BusinessException("Invalid reservation confirmation");
        }
        this.reservedCount -= count;
        this.soldCount += count;
    }
}
