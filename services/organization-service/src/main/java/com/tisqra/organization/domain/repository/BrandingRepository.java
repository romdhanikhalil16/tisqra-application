package com.tisqra.organization.domain.repository;

import com.tisqra.organization.domain.model.Branding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Branding repository interface
 */
@Repository
public interface BrandingRepository extends JpaRepository<Branding, UUID> {

    Optional<Branding> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationId(UUID organizationId);
}
