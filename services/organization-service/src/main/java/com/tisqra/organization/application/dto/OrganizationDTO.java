package com.tisqra.organization.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Organization Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UUID ownerId;
    private String website;
    private String phone;
    private String email;
    private String logoUrl;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
