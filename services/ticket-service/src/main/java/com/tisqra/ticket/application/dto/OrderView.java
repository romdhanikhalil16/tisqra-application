package com.tisqra.ticket.application.dto;

import com.tisqra.common.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public class OrderView {
    private UUID id;
    private UUID userId;
    private UUID eventId;
    private OrderStatus status;
    private List<OrderItemView> items;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItemView> getItems() {
        return items;
    }

    public void setItems(List<OrderItemView> items) {
        this.items = items;
    }
}

