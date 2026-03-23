package com.tisqra.order.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Item domain entity
 * Represents an item in an order (tickets)
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(nullable = false)
    private UUID ticketCategoryId;

    @Column(nullable = false, length = 150)
    private String ticketCategoryName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Business methods
    public BigDecimal calculateTotal() {
        this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        return totalPrice;
    }

    @PrePersist
    @PreUpdate
    private void prePersist() {
        calculateTotal();
    }
}
