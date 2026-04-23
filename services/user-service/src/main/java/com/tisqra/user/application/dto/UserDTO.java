package com.tisqra.user.application.dto;

import com.tisqra.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID organizationId;
    private UserRole role;
    private Boolean isActive;
    private Boolean emailVerified;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
