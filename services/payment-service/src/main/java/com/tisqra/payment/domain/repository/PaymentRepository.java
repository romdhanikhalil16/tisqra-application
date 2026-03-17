package com.tisqra.payment.domain.repository;

import com.tisqra.common.enums.PaymentStatus;
import com.tisqra.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment repository interface
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    List<Payment> findByStatus(PaymentStatus status);
}
