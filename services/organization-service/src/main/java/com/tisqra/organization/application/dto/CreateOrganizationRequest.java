package com.tisqra.organization.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Create organization request.
 */
public record CreateOrganizationRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    String phone,
    String address,
    String city,
    String country
) {
}

