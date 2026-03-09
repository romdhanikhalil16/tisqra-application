package com.tisqra.organization.domain.repository;

import com.tisqra.organization.domain.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subscription plan repository interface
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findByCode(String code);

    List<SubscriptionPlan> findByIsActiveTrueOrderBySortOrderAsc();

    boolean existsByCode(String code);
}
