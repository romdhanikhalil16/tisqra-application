package com.tisqra.order.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.order.application.dto.CreateOrderRequest;
import com.tisqra.order.application.dto.OrderDTO;
import com.tisqra.order.application.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order REST Controller
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<OrderDTO>builder().success(true).data(order).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable UUID id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder().success(true).data(order).build());
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder().success(true).data(order).build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getUserOrders(
            @PathVariable UUID userId,
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder().success(true).data(orders).build());
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get event orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getEventOrders(
            @PathVariable UUID eventId,
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.getEventOrders(eventId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder().success(true).data(orders).build());
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order (after payment)")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.confirmOrder(id);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder().success(true).data(order).build());
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete order (tickets generated)")
    public ResponseEntity<ApiResponse<OrderDTO>> completeOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.completeOrder(id);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder().success(true).data(order).build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<Void>> refundOrder(@PathVariable UUID id) {
        orderService.refundOrder(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}
