package com.tisqra.payment.infrastructure.gateway;

import com.tisqra.common.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Mock Payment Gateway
 * Simulates payment processing for demonstration purposes
 */
@Slf4j
@Component
public class MockPaymentGateway {

    @Value("${payment.gateway.success-rate:0.9}")
    private double successRate;

    private final Random random = new Random();

    /**
     * Process a payment (simulated)
     * 
     * @param amount Payment amount
     * @param currency Currency code
     * @param method Payment method
     * @param cardNumber Card number (for credit/debit cards)
     * @return PaymentResult with success status and transaction ID
     */
    public PaymentResult processPayment(
            BigDecimal amount, 
            String currency, 
            PaymentMethod method,
            String cardNumber) {
        
        log.info("Processing payment: {} {} via {}", amount, currency, method);

        // Simulate processing delay
        simulateDelay();

        // Simulate success/failure based on configured success rate
        boolean success = random.nextDouble() < successRate;

        if (success) {
            String transactionId = generateTransactionId();
            log.info("Payment processed successfully: {}", transactionId);
            return PaymentResult.success(transactionId, "Payment successful");
        } else {
            String failureReason = selectRandomFailureReason();
            log.warn("Payment failed: {}", failureReason);
            return PaymentResult.failure(failureReason);
        }
    }

    /**
     * Process a refund (simulated)
     * 
     * @param originalTransactionId Original payment transaction ID
     * @param amount Refund amount
     * @param currency Currency code
     * @return PaymentResult with success status and refund ID
     */
    public PaymentResult processRefund(
            String originalTransactionId,
            BigDecimal amount,
            String currency) {
        
        log.info("Processing refund: {} {} for transaction {}", amount, currency, originalTransactionId);

        // Simulate processing delay
        simulateDelay();

        // Refunds are always successful in mock gateway
        String refundId = generateRefundId();
        log.info("Refund processed successfully: {}", refundId);
        return PaymentResult.success(refundId, "Refund successful");
    }

    /**
     * Verify a payment transaction (simulated)
     */
    public boolean verifyPayment(String transactionId) {
        log.debug("Verifying payment: {}", transactionId);
        // In mock gateway, all transaction IDs starting with "TXN-" are valid
        return transactionId != null && transactionId.startsWith("TXN-");
    }

    private void simulateDelay() {
        try {
            // Simulate 500ms to 2000ms processing time
            Thread.sleep(500 + random.nextInt(1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
    }

    private String generateRefundId() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
    }

    private String selectRandomFailureReason() {
        String[] reasons = {
            "Insufficient funds",
            "Card declined",
            "Invalid card number",
            "Card expired",
            "Transaction limit exceeded",
            "Suspicious activity detected",
            "Network error"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    /**
     * Payment result value object
     */
    public static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;

        private PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        public static PaymentResult success(String transactionId, String message) {
            return new PaymentResult(true, transactionId, message);
        }

        public static PaymentResult failure(String message) {
            return new PaymentResult(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }
}
