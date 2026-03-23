package com.tisqra.order.domain.model;

import com.tisqra.common.enums.OrderStatus;
import com.tisqra.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order domain entity
 * Represents a ticket purchase order
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "orderNumber"),
    @Index(name = "idx_order_user_id", columnList = "userId"),
    @Index(name = "idx_order_event_id", columnList = "eventId"),
    @Index(name = "idx_order_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(length = 50)
    private String promoCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime confirmedAt;

    // Business methods
    public BigDecimal calculateTotal() {
        this.subtotal = items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = subtotal.subtract(discountAmount);
        return totalAmount;
    }

    public void applyDiscount(BigDecimal discount, String promoCode) {
        if (discount.compareTo(subtotal) > 0) {
            throw new BusinessException("Discount cannot exceed subtotal");
        }
        this.discountAmount = discount;
        this.promoCode = promoCode;
        calculateTotal();
    }

    public void confirmPayment() {
        if (status != OrderStatus.PENDING) {
            throw new BusinessException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel completed order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void refund() {
        if (status != OrderStatus.COMPLETED) {
            throw new BusinessException("Only completed orders can be refunded");
        }
        this.status = OrderStatus.REFUNDED;
    }

    public void complete() {
        if (status != OrderStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed orders can be completed");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public boolean isExpired() {
        return status == OrderStatus.PENDING && LocalDateTime.now().isAfter(expiresAt);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotal();
    }

    public Integer getTotalTickets() {
        return items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
}
