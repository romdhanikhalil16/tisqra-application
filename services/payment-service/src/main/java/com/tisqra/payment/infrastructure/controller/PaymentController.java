package com.tisqra.payment.infrastructure.controller;

import com.tisqra.payment.application.dto.PaymentDTO;
import com.tisqra.payment.application.dto.ProcessPaymentRequest;
import com.tisqra.payment.application.dto.RefundRequest;
import com.tisqra.payment.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Payment REST Controller
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing APIs")
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process a payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        PaymentDTO payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable UUID id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment for an order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentDTO> getPaymentByOrderId(@PathVariable UUID orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Process a refund")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<Void> processRefund(
            @PathVariable UUID id,
            @Valid @RequestBody RefundRequest request) {
        paymentService.processRefund(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify/{providerPaymentId}")
    @Operation(summary = "Verify a payment transaction")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<Boolean> verifyPayment(@PathVariable String providerPaymentId) {
        boolean verified = paymentService.verifyPayment(providerPaymentId);
        return ResponseEntity.ok(verified);
    }
}
