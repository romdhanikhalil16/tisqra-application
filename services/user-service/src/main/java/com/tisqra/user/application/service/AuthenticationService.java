package com.tisqra.user.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.user.application.dto.*;
import com.tisqra.user.application.mapper.UserMapper;
import com.tisqra.user.domain.model.User;
import com.tisqra.user.domain.repository.UserRepository;
import com.tisqra.user.infrastructure.keycloak.KeycloakAdminClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication service - Handles user registration, login, and password management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AuditLogService auditLogService;

    @Transactional
    public UserDTO register(RegisterUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }

        // Create user in Keycloak
        String keycloakId = keycloakAdminClient.createUser(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            request.getRole()
        );

        // Create user in our database
        User user = User.builder()
            .email(request.getEmail())
            .keycloakId(keycloakId)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .role(request.getRole())
            .isActive(true)
            .emailVerified(false)
            .build();

        user = userRepository.save(user);

        // Send verification email (handled by notification service via Kafka)
        // TODO: Publish EmailVerificationEvent to Kafka

        auditLogService.logAction(user.getId(), "USER_REGISTERED", 
            "User registered with email: " + user.getEmail(), null, null);

        log.info("User registered successfully with ID: {}", user.getId());
        return userMapper.toDTO(user);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        // Authenticate with Keycloak
        LoginResponse loginResponse = keycloakAdminClient.authenticate(
            request.getEmail(),
            request.getPassword()
        );

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new BusinessException("User account is deactivated");
        }

        // Update last login time
        user.updateLastLogin();
        userRepository.save(user);

        // Set user in response
        loginResponse.setUser(userMapper.toDTO(user));

        auditLogService.logAction(user.getId(), "USER_LOGIN", "User logged in successfully", null, null);

        log.info("User logged in successfully: {}", request.getEmail());
        return loginResponse;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found"));

        // Generate reset token in Keycloak
        String resetToken = keycloakAdminClient.generatePasswordResetToken(user.getKeycloakId());

        // Send password reset email (via Kafka to notification service)
        // TODO: Publish PasswordResetEvent to Kafka with resetToken

        auditLogService.logAction(user.getId(), "PASSWORD_RESET_REQUESTED", 
            "Password reset requested", null, null);

        log.info("Password reset email sent to: {}", email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password with token");

        // Validate token and reset password in Keycloak
        String keycloakId = keycloakAdminClient.resetPassword(request.getToken(), request.getNewPassword());

        // Find user and log action
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new BusinessException("User not found"));

        auditLogService.logAction(user.getId(), "PASSWORD_RESET", 
            "Password reset successfully", null, null);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String verificationToken) {
        log.info("Verifying email with token");

        // Verify email in Keycloak
        String keycloakId = keycloakAdminClient.verifyEmail(verificationToken);

        // Update user in database
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new BusinessException("User not found"));

        user.verifyEmail();
        userRepository.save(user);

        auditLogService.logAction(user.getId(), "EMAIL_VERIFIED", 
            "Email verified successfully", null, null);

        log.info("Email verified for user: {}", user.getEmail());
    }

    public void logout(UUID userId) {
        log.info("User logout: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found"));

        // Revoke tokens in Keycloak
        keycloakAdminClient.revokeUserSessions(user.getKeycloakId());

        auditLogService.logAction(userId, "USER_LOGOUT", "User logged out", null, null);

        log.info("User logged out successfully");
    }
}
