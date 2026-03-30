package com.tisqra.order.application.service;

import com.tisqra.common.ApiResponse;
import com.tisqra.common.enums.OrderStatus;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.OrderCreatedEvent;
import com.tisqra.kafka.events.PaymentCompletedEvent;
import com.tisqra.kafka.events.PaymentFailedEvent;
import com.tisqra.order.application.dto.CreateOrderRequest;
import com.tisqra.order.application.dto.OrderDTO;
import com.tisqra.order.application.mapper.OrderMapper;
import com.tisqra.order.domain.model.Order;
import com.tisqra.order.domain.model.OrderItem;
import com.tisqra.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Order service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {} and event: {}", request.getUserId(), request.getEventId());

        // Generate unique order number
        String orderNumber = generateOrderNumber();

        // Fetch ticket category details and validate availability
        Order order = Order.builder()
            .orderNumber(orderNumber)
            .userId(request.getUserId())
            .eventId(request.getEventId())
            .status(OrderStatus.PENDING)
            .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 minutes to complete payment
            .build();

        // Add order items
        for (var itemRequest : request.getItems()) {
            // Fetch ticket category details from event service
            Map<String, Object> categoryDetails = fetchTicketCategoryDetails(itemRequest.getTicketCategoryId());
            
            BigDecimal unitPrice = new BigDecimal(categoryDetails.get("price").toString());
            String categoryName = categoryDetails.get("name").toString();

            // Reserve tickets
            reserveTickets(itemRequest.getTicketCategoryId(), itemRequest.getQuantity());

            OrderItem item = OrderItem.builder()
                .ticketCategoryId(itemRequest.getTicketCategoryId())
                .ticketCategoryName(categoryName)
                .quantity(itemRequest.getQuantity())
                .unitPrice(unitPrice)
                .build();

            order.addItem(item);
        }

        // Apply promo code if provided
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            BigDecimal discount = validateAndCalculatePromoDiscount(
                request.getPromoCode(), 
                request.getEventId(), 
                order.getSubtotal()
            );
            order.applyDiscount(discount, request.getPromoCode());
        }

        order.calculateTotal();
        order = orderRepository.save(order);

        // Publish Kafka event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUserId())
            .eventId(order.getEventId())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .ticketCount(order.getTotalTickets())
            .createdAt(order.getCreatedAt())
            .build();
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event);

        log.info("Order created: {}", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    public OrderDTO getOrderById(UUID id) {
        log.debug("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.toDTO(order);
    }

    public OrderDTO getOrderByNumber(String orderNumber) {
        log.debug("Fetching order by number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return orderMapper.toDTO(order);
    }

    public Page<OrderDTO> getUserOrders(UUID userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId, pageable)
            .map(orderMapper::toDTO);
    }

    public Page<OrderDTO> getEventOrders(UUID eventId, Pageable pageable) {
        log.debug("Fetching orders for event: {}", eventId);
        return orderRepository.findByEventId(eventId, pageable)
            .map(orderMapper::toDTO);
    }

    @Transactional
    public OrderDTO confirmOrder(UUID orderId) {
        log.info("Confirming order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.isExpired()) {
            throw new BusinessException("Order has expired");
        }

        order.confirmPayment();
        order = orderRepository.save(order);

        log.info("Order confirmed: {}", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    @Transactional
    public OrderDTO completeOrder(UUID orderId) {
        log.info("Completing order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.complete();
        order = orderRepository.save(order);

        log.info("Order completed: {}", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Release reserved tickets
        for (OrderItem item : order.getItems()) {
            releaseTickets(item.getTicketCategoryId(), item.getQuantity());
        }

        order.cancel();
        orderRepository.save(order);

        log.info("Order cancelled: {}", order.getOrderNumber());
    }

    @Transactional
    public void refundOrder(UUID orderId) {
        log.info("Refunding order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.refund();
        orderRepository.save(order);

        log.info("Order refunded: {}", order.getOrderNumber());
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void expireOrders() {
        log.debug("Checking for expired orders");
        
        List<Order> expiredOrders = orderRepository.findExpiredOrders(LocalDateTime.now());
        
        for (Order order : expiredOrders) {
            log.info("Expiring order: {}", order.getOrderNumber());
            
            // Release reserved tickets
            for (OrderItem item : order.getItems()) {
                releaseTickets(item.getTicketCategoryId(), item.getQuantity());
            }
            
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
        }
        
        if (!expiredOrders.isEmpty()) {
            log.info("Expired {} orders", expiredOrders.size());
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }

    private Map<String, Object> fetchTicketCategoryDetails(UUID categoryId) {
        try {
            String url = "http://event-service/api/ticket-categories/" + categoryId;
            ApiResponse<?> response = restTemplate.getForObject(url, ApiResponse.class);
            if (response == null || response.getData() == null) {
                throw new BusinessException("Missing ticket category details");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getData();
            return data;
        } catch (Exception e) {
            log.error("Failed to fetch ticket category details", e);
            throw new BusinessException("Failed to fetch ticket details");
        }
    }

    private void reserveTickets(UUID categoryId, Integer quantity) {
        try {
            String url = "http://event-service/api/ticket-categories/" + categoryId + "/reserve?quantity=" + quantity;
            restTemplate.postForObject(url, null, ApiResponse.class);
        } catch (Exception e) {
            log.error("Failed to reserve tickets", e);
            throw new BusinessException("Failed to reserve tickets. They may be sold out.");
        }
    }

    private void releaseTickets(UUID categoryId, Integer quantity) {
        try {
            String url = "http://event-service/api/ticket-categories/" + categoryId + "/release?quantity=" + quantity;
            restTemplate.postForObject(url, null, ApiResponse.class);
        } catch (Exception e) {
            log.error("Failed to release tickets", e);
        }
    }

    private BigDecimal validateAndCalculatePromoDiscount(String code, UUID eventId, BigDecimal amount) {
        try {
            String url = "http://event-service/api/promo-codes/validate?code=" + code + "&eventId=" + eventId;
            ApiResponse<?> response = restTemplate.getForObject(url, ApiResponse.class);
            if (response == null || response.getData() == null) {
                throw new BusinessException("Invalid promo code");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> promoData = (Map<String, Object>) response.getData();
            
            String discountType = promoData.get("discountType").toString();
            BigDecimal discountValue = new BigDecimal(promoData.get("discountValue").toString());
            
            if ("PERCENTAGE".equals(discountType)) {
                return amount.multiply(discountValue).divide(new BigDecimal("100"));
            } else {
                return discountValue.min(amount);
            }
        } catch (Exception e) {
            log.error("Failed to validate promo code", e);
            throw new BusinessException("Invalid promo code");
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "order-service")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PAYMENT_COMPLETED for order: {}", event.getOrderId());
        confirmOrder(event.getOrderId());
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PAYMENT_FAILED for order: {}", event.getOrderId());
        cancelOrder(event.getOrderId());
    }
}
