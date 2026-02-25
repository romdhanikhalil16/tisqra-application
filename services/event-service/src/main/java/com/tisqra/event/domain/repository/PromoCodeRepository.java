package com.tisqra.event.domain.repository;

import com.tisqra.event.domain.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Promo Code repository interface
 */
@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, UUID> {

    Optional<PromoCode> findByCodeAndEventId(String code, UUID eventId);

    List<PromoCode> findByEventId(UUID eventId);

    boolean existsByCode(String code);
}
