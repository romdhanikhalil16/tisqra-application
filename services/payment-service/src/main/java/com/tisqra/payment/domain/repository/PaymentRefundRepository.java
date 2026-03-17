package com.tisqra.payment.domain.repository;

import com.tisqra.payment.domain.model.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Payment Refund repository interface
 */
@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, UUID> {

    List<PaymentRefund> findByPaymentId(UUID paymentId);
}
