package com.tisqra.organization.domain.repository;

import com.tisqra.organization.domain.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByEmail(String email);
}

