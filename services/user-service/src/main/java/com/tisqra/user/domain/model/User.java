package com.tisqra.user.domain.model;

import com.tisqra.common.enums.UserRole;
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
 * User domain entity
 * Represents a user in the system
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_keycloak_id", columnList = "keycloakId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String keycloakId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(length = 255)
    private String profileImageUrl;

    @Column(length = 255, unique = true)
    private String verificationToken;

    @Column
    private LocalDateTime verificationTokenExpiresAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt;

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.verificationToken = null;
        this.verificationTokenExpiresAt = null;
    }

    public void issueVerificationToken(String token, LocalDateTime expiresAt) {
        this.verificationToken = token;
        this.verificationTokenExpiresAt = expiresAt;
    }

    public boolean isVerificationTokenExpired() {
        return verificationTokenExpiresAt == null || verificationTokenExpiresAt.isBefore(LocalDateTime.now());
    }
}
