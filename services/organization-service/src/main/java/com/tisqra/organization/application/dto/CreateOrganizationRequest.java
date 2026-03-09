package com.tisqra.organization.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating an organization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String description;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

    private String website;

    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String logoUrl;

    @NotBlank(message = "Subscription plan code is required")
    private String subscriptionPlanCode;
}
