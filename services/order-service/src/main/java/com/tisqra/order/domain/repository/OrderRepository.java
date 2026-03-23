package com.tisqra.order.domain.repository;

import com.tisqra.common.enums.OrderStatus;
import com.tisqra.order.domain.model.Order;
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
 * Order repository interface
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    Page<Order> findByEventId(UUID eventId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.expiresAt < :now")
    List<Order> findExpiredOrders(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.eventId = :eventId AND o.status IN ('CONFIRMED', 'COMPLETED')")
    Long countConfirmedOrdersByEventId(@Param("eventId") UUID eventId);
}
