package com.tisqra.organization.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.organization.application.dto.SubscriptionDTO;
import com.tisqra.organization.application.dto.SubscriptionPlanDTO;
import com.tisqra.organization.application.mapper.SubscriptionMapper;
import com.tisqra.organization.application.mapper.SubscriptionPlanMapper;
import com.tisqra.organization.domain.model.Subscription;
import com.tisqra.organization.domain.model.SubscriptionPlan;
import com.tisqra.organization.domain.model.SubscriptionStatus;
import com.tisqra.organization.domain.repository.SubscriptionPlanRepository;
import com.tisqra.organization.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Subscription service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Cacheable(value = "subscriptions", key = "#organizationId")
    public SubscriptionDTO getOrganizationSubscription(UUID organizationId) {
        log.debug("Fetching subscription for organization: {}", organizationId);
        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription", "organizationId", organizationId));
        return subscriptionMapper.toDTO(subscription);
    }

    public List<SubscriptionPlanDTO> getAllPlans() {
        log.debug("Fetching all active subscription plans");
        return subscriptionPlanRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
            .map(subscriptionPlanMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void incrementEventCount(UUID organizationId) {
        log.debug("Incrementing event count for organization: {}", organizationId);

        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription", "organizationId", organizationId));

        if (!subscription.canCreateEvent()) {
            throw new BusinessException("Event creation limit reached for current subscription plan");
        }

        subscription.incrementEventCount();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void resetMonthlyEventCounts() {
        log.info("Resetting monthly event counts for all subscriptions");
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        subscriptions.forEach(Subscription::resetMonthlyEventCount);
        subscriptionRepository.saveAll(subscriptions);
    }

    @Transactional
    public SubscriptionDTO upgradeSubscription(UUID organizationId, String planCode) {
        log.info("Upgrading subscription for organization: {} to plan: {}", organizationId, planCode);

        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription", "organizationId", organizationId));

        SubscriptionPlan newPlan = subscriptionPlanRepository.findByCode(planCode)
            .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "code", planCode));

        subscription.setPlan(newPlan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(null);
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription upgraded successfully");
        return subscriptionMapper.toDTO(subscription);
    }

    @Transactional
    public void cancelSubscription(UUID organizationId) {
        log.info("Cancelling subscription for organization: {}", organizationId);

        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription", "organizationId", organizationId));

        subscription.cancel();
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled");
    }

    public boolean canOrganizationCreateEvent(UUID organizationId) {
        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElse(null);
        return subscription != null && subscription.canCreateEvent();
    }
}
