package com.tisqra.user.domain.repository;

import com.tisqra.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User repository interface
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);
}
