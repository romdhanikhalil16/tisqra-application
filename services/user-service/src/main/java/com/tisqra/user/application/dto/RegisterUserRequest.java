package com.tisqra.user.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.tisqra.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration (signup)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    /**
     * Optional username. If not provided, backend will default to email.
     * This exists to support UIs that treat username separately from email.
     */
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    @JsonAlias("userRole")
    @Builder.Default
    private String role = UserRole.GUEST.name(); // Default to GUEST for public signup

    public UserRole resolveRole() {
        if (role == null || role.trim().isEmpty()) {
            return UserRole.GUEST;
        }

        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Unsupported role '" + role + "'. Allowed values: SUPER_ADMIN, ADMIN_ORG, SCANNER, GUEST"
            );
        }
    }
}
