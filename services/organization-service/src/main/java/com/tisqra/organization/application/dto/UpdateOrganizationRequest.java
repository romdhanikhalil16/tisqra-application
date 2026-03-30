package com.tisqra.organization.application.dto;

import jakarta.validation.constraints.Email;

/**
 * Update organization request.
 */
public record UpdateOrganizationRequest(
    String name,
    @Email String email,
    String phone,
    String address,
    String city,
    String country,
    String domain
) {
}

