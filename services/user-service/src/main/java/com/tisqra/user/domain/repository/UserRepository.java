package com.tisqra.user.domain.repository;

import com.tisqra.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * User repository interface
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByEmailAndOrganizationId(String email, UUID organizationId);

    Page<User> findByOrganizationId(UUID organizationId, Pageable pageable);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);

    long countByRoleAndIsActiveTrue(com.tisqra.common.enums.UserRole role);
}
