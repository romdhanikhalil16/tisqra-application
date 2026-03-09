package com.tisqra.organization.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Branding domain entity
 * Stores organization branding and theme settings
 */
@Entity
@Table(name = "brandings", indexes = {
    @Index(name = "idx_branding_org_id", columnList = "organizationId", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID organizationId;

    @Column(length = 7)
    @Builder.Default
    private String primaryColor = "#007BFF";

    @Column(length = 7)
    @Builder.Default
    private String secondaryColor = "#6C757D";

    @Column(length = 7)
    @Builder.Default
    private String accentColor = "#28A745";

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String bannerUrl;

    @Column(length = 500)
    private String faviconUrl;

    @Column(length = 100)
    @Builder.Default
    private String fontFamily = "Arial, sans-serif";

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business methods
    public String applyToTicket() {
        return String.format("background-color: %s; color: %s;", primaryColor, secondaryColor);
    }

    public String applyToEmail() {
        return String.format("color: %s; font-family: %s;", primaryColor, fontFamily);
    }
}
