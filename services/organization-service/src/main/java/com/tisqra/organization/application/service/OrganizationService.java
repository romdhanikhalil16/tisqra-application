package com.tisqra.organization.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ConflictException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.OrganizationCreatedEvent;
import com.tisqra.organization.application.dto.CreateOrganizationRequest;
import com.tisqra.organization.application.dto.OrganizationDTO;
import com.tisqra.organization.application.dto.UpdateOrganizationRequest;
import com.tisqra.organization.domain.model.Organization;
import com.tisqra.organization.domain.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public OrganizationDTO createOrganization(CreateOrganizationRequest request) {
        organizationRepository.findByEmail(request.email()).ifPresent(org -> {
            throw new ConflictException("Organization email already exists: " + request.email());
        });

        UUID ownerId = extractOwnerIdFromJwt();

        Organization organization = Organization.builder()
            .name(request.name())
            .email(request.email())
            .phone(request.phone())
            .address(request.address())
            .city(request.city())
            .country(request.country())
            .ownerId(ownerId)
            .build();

        organization = organizationRepository.save(organization);

        publishOrganizationCreatedEvent(organization);

        return toDTO(organization);
    }

    public Page<OrganizationDTO> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(this::toDTO);
    }

    public OrganizationDTO getOrganizationById(UUID id) {
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        return toDTO(organization);
    }

    @Transactional
    public OrganizationDTO updateOrganization(UUID id, UpdateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        if (request.name() != null) {
            organization.setName(request.name());
        }
        if (request.email() != null && !request.email().equalsIgnoreCase(organization.getEmail())) {
            organizationRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new ConflictException("Organization email already exists: " + request.email());
            });
            organization.setEmail(request.email());
        }
        if (request.phone() != null) organization.setPhone(request.phone());
        if (request.address() != null) organization.setAddress(request.address());
        if (request.city() != null) organization.setCity(request.city());
        if (request.country() != null) organization.setCountry(request.country());
        if (request.domain() != null) organization.setDomain(request.domain());

        organization = organizationRepository.save(organization);
        return toDTO(organization);
    }

    public boolean canCreateEvent(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));

        return organization.canCreateEvent();
    }

    @Transactional
    public void incrementEventCount(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));

        if (!organization.canCreateEvent()) {
            throw new BusinessException("Organization has reached its event creation limit");
        }

        organization.incrementEventCount();
        organizationRepository.save(organization);
    }

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
            .id(org.getId())
            .name(org.getName())
            .email(org.getEmail())
            .phone(org.getPhone())
            .address(org.getAddress())
            .city(org.getCity())
            .country(org.getCountry())
            .domain(org.getDomain())
            .ownerId(org.getOwnerId())
            .subscriptionPlan(org.getSubscriptionPlan())
            .maxEvents(org.getMaxEvents())
            .eventCount(org.getEventCount())
            .build();
    }

    private UUID extractOwnerIdFromJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return null;
            }

            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String sub = jwt.getClaimAsString("sub");
                if (sub != null) {
                    return UUID.fromString(sub);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract ownerId from JWT", e);
        }

        return null;
    }

    private void publishOrganizationCreatedEvent(Organization org) {
        try {
            OrganizationCreatedEvent event = OrganizationCreatedEvent.builder()
                .organizationId(org.getId())
                .name(org.getName())
                .ownerId(org.getOwnerId())
                .subscriptionPlan(org.getSubscriptionPlan())
                .createdAt(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();

            kafkaTemplate.send(KafkaTopics.ORGANIZATION_CREATED, event);
        } catch (Exception e) {
            // Not fatal for API correctness
            log.warn("Failed to publish ORGANIZATION_CREATED event", e);
        }
    }
}

