package com.tisqra.event.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Event Schedule domain entity
 * Represents agenda items for an event
 */
@Entity
@Table(name = "event_schedules", indexes = {
    @Index(name = "idx_schedule_event_id", columnList = "eventId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String speaker;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
