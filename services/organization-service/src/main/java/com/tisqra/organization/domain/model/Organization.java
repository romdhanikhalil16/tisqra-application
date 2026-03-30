package com.tisqra.organization.domain.model;

import com.tisqra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "organizations")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(length = 150)
    private String domain;

    @Column(nullable = true)
    private UUID ownerId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String subscriptionPlan = "BASIC";

    @Column(nullable = false)
    @Builder.Default
    private Integer maxEvents = 5;

    @Column(nullable = false)
    @Builder.Default
    private Integer eventCount = 0;

    public boolean canCreateEvent() {
        Integer count = eventCount == null ? 0 : eventCount;
        return count < maxEvents;
    }

    public void incrementEventCount() {
        int count = eventCount == null ? 0 : eventCount;
        eventCount = count + 1;
    }
}

