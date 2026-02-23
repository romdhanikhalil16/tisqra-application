package com.tisqra.user.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log domain entity
 * Tracks user actions for security and compliance
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "userId"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
