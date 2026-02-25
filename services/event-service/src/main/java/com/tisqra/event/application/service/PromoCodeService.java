package com.tisqra.event.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.event.application.dto.PromoCodeDTO;
import com.tisqra.event.application.mapper.PromoCodeMapper;
import com.tisqra.event.domain.model.DiscountType;
import com.tisqra.event.domain.model.PromoCode;
import com.tisqra.event.domain.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Promo code service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional
    public PromoCodeDTO createPromoCode(UUID eventId, String code, DiscountType type, 
                                       BigDecimal value, Integer maxUses,
                                       LocalDateTime validFrom, LocalDateTime validUntil) {
        log.info("Creating promo code: {} for event: {}", code, eventId);

        if (promoCodeRepository.existsByCode(code)) {
            throw new BusinessException("Promo code already exists");
        }

        PromoCode promoCode = PromoCode.builder()
            .eventId(eventId)
            .code(code.toUpperCase())
            .discountType(type)
            .discountValue(value)
            .maxUses(maxUses)
            .validFrom(validFrom)
            .validUntil(validUntil)
            .build();

        promoCode = promoCodeRepository.save(promoCode);
        log.info("Promo code created: {}", code);
        return promoCodeMapper.toDTO(promoCode);
    }

    public PromoCodeDTO validatePromoCode(String code, UUID eventId) {
        log.debug("Validating promo code: {} for event: {}", code, eventId);

        PromoCode promoCode = promoCodeRepository.findByCodeAndEventId(code.toUpperCase(), eventId)
            .orElseThrow(() -> new BusinessException("Invalid promo code"));

        if (!promoCode.isValid()) {
            throw new BusinessException("Promo code is expired or has reached usage limit");
        }

        return promoCodeMapper.toDTO(promoCode);
    }

    public List<PromoCodeDTO> getEventPromoCodes(UUID eventId) {
        log.debug("Fetching promo codes for event: {}", eventId);
        return promoCodeRepository.findByEventId(eventId).stream()
            .map(promoCodeMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void incrementPromoCodeUsage(UUID promoCodeId) {
        log.debug("Incrementing usage for promo code: {}", promoCodeId);

        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
            .orElseThrow(() -> new ResourceNotFoundException("PromoCode", "id", promoCodeId));

        promoCode.incrementUsage();
        promoCodeRepository.save(promoCode);
    }

    @Transactional
    public void deactivatePromoCode(UUID promoCodeId) {
        log.info("Deactivating promo code: {}", promoCodeId);

        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
            .orElseThrow(() -> new ResourceNotFoundException("PromoCode", "id", promoCodeId));

        promoCode.deactivate();
        promoCodeRepository.save(promoCode);
    }
}
