package com.tisqra.organization.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Branding Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingDTO {
    private UUID id;
    private UUID organizationId;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String logoUrl;
    private String bannerUrl;
    private String faviconUrl;
    private String fontFamily;
}
