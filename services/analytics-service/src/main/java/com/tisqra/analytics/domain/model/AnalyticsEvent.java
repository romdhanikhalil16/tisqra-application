package com.tisqra.analytics.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Event domain entity
 * Tracks user events for analytics
 */
@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_analytics_event_type", columnList = "eventType"),
    @Index(name = "idx_analytics_aggregate_id", columnList = "aggregateId"),
    @Index(name = "idx_analytics_org_id", columnList = "organizationId"),
    @Index(name = "idx_analytics_occurred_at", columnList = "occurredAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column
    private UUID organizationId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
