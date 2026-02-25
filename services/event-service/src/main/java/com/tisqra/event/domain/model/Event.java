package com.tisqra.event.domain.model;

import com.tisqra.common.enums.EventCategory;
import com.tisqra.common.enums.EventStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Event domain entity
 * Represents an event with location, schedule, and ticket categories
 */
@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_slug", columnList = "slug"),
    @Index(name = "idx_event_org_id", columnList = "organizationId"),
    @Index(name = "idx_event_status", columnList = "status"),
    @Index(name = "idx_event_start_date", columnList = "startDate")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventCategory category;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Embedded
    private Location location;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @Column(length = 500)
    private String bannerImageUrl;

    @Column(length = 500)
    private String thumbnailImageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TicketCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventSchedule> schedule = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime publishedAt;

    // Business methods
    public void publish() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = EventStatus.CANCELLED;
    }

    public void complete() {
        this.status = EventStatus.COMPLETED;
    }

    public void startEvent() {
        this.status = EventStatus.ONGOING;
    }

    public boolean isBookable() {
        return status == EventStatus.PUBLISHED && 
               LocalDateTime.now().isBefore(startDate);
    }

    public Integer getAvailableTickets() {
        return categories.stream()
            .mapToInt(TicketCategory::getAvailableCount)
            .sum();
    }

    public void addCategory(TicketCategory category) {
        categories.add(category);
        category.setEvent(this);
    }

    public void addScheduleItem(EventSchedule scheduleItem) {
        schedule.add(scheduleItem);
        scheduleItem.setEvent(this);
    }
}
