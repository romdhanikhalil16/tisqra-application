package com.tisqra.organization.domain.repository;

import com.tisqra.organization.domain.model.Subscription;
import com.tisqra.organization.domain.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subscription repository interface
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByOrganizationId(UUID organizationId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    Optional<Subscription> findByOrganizationIdAndStatus(UUID organizationId, SubscriptionStatus status);
}
