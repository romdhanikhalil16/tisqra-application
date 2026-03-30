package com.tisqra.organization.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String domain;
    private UUID ownerId;
    private String subscriptionPlan;
    private Integer maxEvents;
    private Integer eventCount;
}

