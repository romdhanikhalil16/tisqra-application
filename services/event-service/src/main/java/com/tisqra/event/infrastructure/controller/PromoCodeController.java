package com.tisqra.event.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.event.application.dto.PromoCodeDTO;
import com.tisqra.event.application.service.PromoCodeService;
import com.tisqra.event.domain.model.DiscountType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Promo Code REST Controller
 */
@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
@Tag(name = "Promo Codes", description = "Promo code management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    @Operation(summary = "Create promo code")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<PromoCodeDTO>> createPromoCode(
            @RequestParam UUID eventId,
            @RequestParam String code,
            @RequestParam DiscountType type,
            @RequestParam BigDecimal value,
            @RequestParam(required = false) Integer maxUses,
            @RequestParam LocalDateTime validFrom,
            @RequestParam LocalDateTime validUntil) {
        PromoCodeDTO promoCode = promoCodeService.createPromoCode(
            eventId, code, type, value, maxUses, validFrom, validUntil);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<PromoCodeDTO>builder().success(true).data(promoCode).build());
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate promo code")
    public ResponseEntity<ApiResponse<PromoCodeDTO>> validatePromoCode(
            @RequestParam String code,
            @RequestParam UUID eventId) {
        PromoCodeDTO promoCode = promoCodeService.validatePromoCode(code, eventId);
        return ResponseEntity.ok(ApiResponse.<PromoCodeDTO>builder().success(true).data(promoCode).build());
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get event promo codes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<List<PromoCodeDTO>>> getEventPromoCodes(@PathVariable UUID eventId) {
        List<PromoCodeDTO> promoCodes = promoCodeService.getEventPromoCodes(eventId);
        return ResponseEntity.ok(ApiResponse.<List<PromoCodeDTO>>builder().success(true).data(promoCodes).build());
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate promo code")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<Void>> deactivatePromoCode(@PathVariable UUID id) {
        promoCodeService.deactivatePromoCode(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}
