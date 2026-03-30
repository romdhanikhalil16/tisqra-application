package com.tisqra.payment.application.service;

import com.tisqra.common.ApiResponse;
import com.tisqra.common.enums.PaymentStatus;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.PaymentCompletedEvent;
import com.tisqra.kafka.events.PaymentFailedEvent;
import com.tisqra.payment.application.dto.PaymentDTO;
import com.tisqra.payment.application.dto.ProcessPaymentRequest;
import com.tisqra.payment.application.dto.RefundRequest;
import com.tisqra.payment.application.mapper.PaymentMapper;
import com.tisqra.payment.domain.model.Payment;
import com.tisqra.payment.domain.model.PaymentRefund;
import com.tisqra.payment.domain.repository.PaymentRefundRepository;
import com.tisqra.payment.domain.repository.PaymentRepository;
import com.tisqra.payment.infrastructure.gateway.MockPaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Payment service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentMapper paymentMapper;
    private final MockPaymentGateway paymentGateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Transactional
    public PaymentDTO processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BusinessException("Payment already exists for this order");
        }

        // Fetch order details
        Map<String, Object> orderData = fetchOrderDetails(request.getOrderId());
        BigDecimal amount = new BigDecimal(orderData.get("totalAmount").toString());
        String currency = orderData.get("currency").toString();
        UUID userId = UUID.fromString(orderData.get("userId").toString());

        // Create payment record
        Payment payment = Payment.builder()
            .orderId(request.getOrderId())
            .amount(amount)
            .currency(currency)
            .method(request.getPaymentMethod())
            .provider("MOCK_GATEWAY")
            .status(PaymentStatus.PENDING)
            .build();

        payment = paymentRepository.save(payment);

        // Process payment with gateway
        payment.process();
        paymentRepository.save(payment);

        MockPaymentGateway.PaymentResult result = paymentGateway.processPayment(
            amount, 
            currency, 
            request.getPaymentMethod(),
            request.getCardNumber()
        );

        if (result.isSuccess()) {
            // Payment succeeded
            payment.complete(result.getTransactionId());
            payment = paymentRepository.save(payment);

            // Publish success event
            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(userId)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getMethod().name())
                .providerPaymentId(payment.getProviderPaymentId())
                .paidAt(payment.getPaidAt())
                .eventId(UUID.randomUUID().toString())
                .build();
            kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, event);

            log.info("Payment completed successfully for order: {}", request.getOrderId());
        } else {
            // Payment failed
            payment.fail(result.getMessage());
            payment = paymentRepository.save(payment);

            // Publish failure event
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(userId)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .failureReason(result.getMessage())
                .failedAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();
            kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, event);

            log.warn("Payment failed for order: {} - {}", request.getOrderId(), result.getMessage());
        }

        return paymentMapper.toDTO(payment);
    }

    public PaymentDTO getPaymentById(UUID id) {
        log.debug("Fetching payment by ID: {}", id);
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return paymentMapper.toDTO(payment);
    }

    public PaymentDTO getPaymentByOrderId(UUID orderId) {
        log.debug("Fetching payment for order: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return paymentMapper.toDTO(payment);
    }

    @Transactional
    public void processRefund(UUID paymentId, RefundRequest request) {
        log.info("Processing refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("Only completed payments can be refunded");
        }

        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("Refund amount cannot exceed payment amount");
        }

        // Create refund record
        PaymentRefund refund = PaymentRefund.builder()
            .paymentId(paymentId)
            .amount(request.getAmount())
            .currency(payment.getCurrency())
            .reason(request.getReason())
            .build();

        refund = paymentRefundRepository.save(refund);

        // Process refund with gateway
        MockPaymentGateway.PaymentResult result = paymentGateway.processRefund(
            payment.getProviderPaymentId(),
            request.getAmount(),
            payment.getCurrency()
        );

        if (result.isSuccess()) {
            refund.process(result.getTransactionId());
            paymentRefundRepository.save(refund);

            // Update payment status
            if (request.getAmount().compareTo(payment.getAmount()) == 0) {
                payment.refund();
            } else {
                payment.partiallyRefund();
            }
            paymentRepository.save(payment);

            // Refund order
            refundOrder(payment.getOrderId());

            log.info("Refund processed successfully for payment: {}", paymentId);
        } else {
            refund.fail();
            paymentRefundRepository.save(refund);
            throw new BusinessException("Refund failed: " + result.getMessage());
        }
    }

    @Transactional
    public boolean verifyPayment(String providerPaymentId) {
        log.debug("Verifying payment: {}", providerPaymentId);
        return paymentGateway.verifyPayment(providerPaymentId);
    }

    private Map<String, Object> fetchOrderDetails(UUID orderId) {
        try {
            String url = "http://order-service/api/orders/" + orderId;
            ApiResponse<?> response = restTemplate.getForObject(url, ApiResponse.class);
            if (response == null || response.getData() == null) {
                throw new BusinessException("Missing order details");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getData();
            return data;
        } catch (Exception e) {
            log.error("Failed to fetch order details", e);
            throw new BusinessException("Failed to fetch order details");
        }
    }

    private void refundOrder(UUID orderId) {
        try {
            String url = "http://order-service/api/orders/" + orderId + "/refund";
            restTemplate.postForObject(url, null, ApiResponse.class);
        } catch (Exception e) {
            log.error("Failed to refund order", e);
        }
    }
}
