package com.tisqra.organization.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.OrganizationCreatedEvent;
import com.tisqra.organization.application.dto.CreateOrganizationRequest;
import com.tisqra.organization.application.dto.OrganizationDTO;
import com.tisqra.organization.application.mapper.OrganizationMapper;
import com.tisqra.organization.domain.model.BillingCycle;
import com.tisqra.organization.domain.model.Organization;
import com.tisqra.organization.domain.model.Subscription;
import com.tisqra.organization.domain.model.SubscriptionPlan;
import com.tisqra.organization.domain.model.SubscriptionStatus;
import com.tisqra.organization.domain.repository.OrganizationRepository;
import com.tisqra.organization.domain.repository.SubscriptionPlanRepository;
import com.tisqra.organization.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Organization service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final OrganizationMapper organizationMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public OrganizationDTO createOrganization(CreateOrganizationRequest request) {
        log.info("Creating organization: {}", request.getName());

        // Generate unique slug
        String slug = generateSlug(request.getName());
        if (organizationRepository.existsBySlug(slug)) {
            throw new BusinessException("Organization with similar name already exists");
        }

        // Get subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(request.getSubscriptionPlanCode())
            .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "code", request.getSubscriptionPlanCode()));

        // Create organization
        Organization organization = organizationMapper.toEntity(request);
        organization.setSlug(slug);
        organization = organizationRepository.save(organization);

        // Create subscription with trial period
        Subscription subscription = Subscription.builder()
            .organizationId(organization.getId())
            .plan(plan)
            .status(SubscriptionStatus.TRIAL)
            .billingCycle(BillingCycle.MONTHLY)
            .startDate(LocalDateTime.now())
            .trialEndDate(LocalDateTime.now().plusDays(30))
            .build();
        subscriptionRepository.save(subscription);

        // Publish event
        OrganizationCreatedEvent event = OrganizationCreatedEvent.builder()
            .organizationId(organization.getId())
            .name(organization.getName())
            .ownerId(organization.getOwnerId())
            .subscriptionPlan(plan.getName())
            .createdAt(organization.getCreatedAt())
            .eventId(UUID.randomUUID().toString())
            .build();
        kafkaTemplate.send(KafkaTopics.ORGANIZATION_CREATED, event);

        log.info("Organization created: {}", organization.getId());
        return organizationMapper.toDTO(organization);
    }

    @Cacheable(value = "organizations", key = "#id")
    public OrganizationDTO getOrganizationById(UUID id) {
        log.debug("Fetching organization by ID: {}", id);
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return organizationMapper.toDTO(organization);
    }

    @Cacheable(value = "organizations", key = "#slug")
    public OrganizationDTO getOrganizationBySlug(String slug) {
        log.debug("Fetching organization by slug: {}", slug);
        Organization organization = organizationRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "slug", slug));
        return organizationMapper.toDTO(organization);
    }

    public List<OrganizationDTO> getOrganizationsByOwner(UUID ownerId) {
        log.debug("Fetching organizations for owner: {}", ownerId);
        return organizationRepository.findByOwnerId(ownerId).stream()
            .map(organizationMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<OrganizationDTO> getAllActiveOrganizations() {
        log.debug("Fetching all active organizations");
        return organizationRepository.findByIsActiveTrue().stream()
            .map(organizationMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "organizations", key = "#id")
    public OrganizationDTO updateOrganization(UUID id, CreateOrganizationRequest request) {
        log.info("Updating organization: {}", id);

        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setWebsite(request.getWebsite());
        organization.setPhone(request.getPhone());
        organization.setEmail(request.getEmail());
        organization.setLogoUrl(request.getLogoUrl());

        organization = organizationRepository.save(organization);
        log.info("Organization updated: {}", id);
        return organizationMapper.toDTO(organization);
    }

    @Transactional
    @CacheEvict(value = "organizations", key = "#id")
    public void verifyOrganization(UUID id) {
        log.info("Verifying organization: {}", id);

        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        organization.verify();
        organizationRepository.save(organization);
        log.info("Organization verified: {}", id);
    }

    @Transactional
    @CacheEvict(value = "organizations", key = "#id")
    public void deactivateOrganization(UUID id) {
        log.info("Deactivating organization: {}", id);

        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        organization.deactivate();
        organizationRepository.save(organization);
        log.info("Organization deactivated: {}", id);
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();

        // Ensure uniqueness
        String finalSlug = slug;
        int counter = 1;
        while (organizationRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + counter++;
        }

        return finalSlug;
    }
}
