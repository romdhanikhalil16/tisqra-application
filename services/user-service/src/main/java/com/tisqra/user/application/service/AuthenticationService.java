package com.tisqra.user.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.enums.UserRole;
import com.tisqra.user.application.dto.*;
import com.tisqra.user.application.mapper.UserMapper;
import com.tisqra.user.domain.model.User;
import com.tisqra.user.domain.repository.UserRepository;
import com.tisqra.user.infrastructure.email.EmailService;
import com.tisqra.user.infrastructure.email.VerificationTokenNormalizer;
import com.tisqra.user.infrastructure.keycloak.KeycloakAdminClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication service - Handles user registration, login, and password management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final long EMAIL_VERIFICATION_EXPIRATION_HOURS = 24L;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;

    /**
     * Registration: create Keycloak user, persist local user + token in a short DB transaction,
     * then send SMTP mail after commit so a mail failure does not roll back the user row.
     */
    public UserDTO register(RegisterUserRequest request) {
        final String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("Registering new user with email: {}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("User with email " + normalizedEmail + " already exists");
        }

        final UserRole role;
        try {
            role = request.resolveRole();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ex.getMessage());
        }

        final String username = Optional.ofNullable(request.getUsername())
            .map(String::trim)
            .filter(u -> !u.isEmpty())
            .orElse(normalizedEmail);

        String keycloakId = keycloakAdminClient.createUser(
            username,
            normalizedEmail,
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            role
        );

        User saved = transactionTemplate.execute(status -> {
            User user = User.builder()
                .email(normalizedEmail)
                .keycloakId(keycloakId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .emailVerified(false)
                .build();
            user.issueVerificationToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(EMAIL_VERIFICATION_EXPIRATION_HOURS)
            );
            return userRepository.save(user);
        });

        emailService.sendVerificationEmail(saved.getEmail(), saved.getFirstName(), saved.getVerificationToken());

        safeAudit(saved.getId(), "USER_REGISTERED", "User registered with email: " + saved.getEmail());

        log.info("User registered successfully with ID: {}", saved.getId());
        return userMapper.toDTO(saved);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        final String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("User login attempt: {}", normalizedEmail);

        LoginResponse loginResponse = keycloakAdminClient.authenticate(
            normalizedEmail,
            request.getPassword()
        );

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new BusinessException("User account is deactivated");
        }

        user.updateLastLogin();
        userRepository.save(user);

        loginResponse.setUser(userMapper.toDTO(user));

        safeAudit(user.getId(), "USER_LOGIN", "User logged in successfully");

        log.info("User logged in successfully: {}", normalizedEmail);
        return loginResponse;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        final String normalizedEmail = normalizeEmail(email);
        log.info("Password reset requested for email: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

        keycloakAdminClient.generatePasswordResetToken(user.getKeycloakId());

        safeAudit(user.getId(), "PASSWORD_RESET_REQUESTED", "Password reset requested");

        log.info("Password reset email sent to: {}", normalizedEmail);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password with token");

        String keycloakId = keycloakAdminClient.resetPassword(request.getToken(), request.getNewPassword());

        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new BusinessException("User not found"));

        safeAudit(user.getId(), "PASSWORD_RESET", "Password reset successfully");

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    /**
     * Validates token from DB, syncs Keycloak, marks user verified, clears token (single use).
     */
    @Transactional
    public void verifyEmail(String verificationToken) {
        log.info("Verifying email with token");

        String token = VerificationTokenNormalizer.normalize(verificationToken);
        if (token.isEmpty()) {
            throw new BusinessException("Verification token is required");
        }

        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new BusinessException("Invalid verification token"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Email is already verified");
        }
        if (user.isVerificationTokenExpired()) {
            throw new BusinessException("Verification token has expired");
        }

        user.verifyEmail();
        userRepository.save(user);

        try {
            keycloakAdminClient.markEmailVerified(user.getKeycloakId());
        } catch (Exception e) {
            log.warn("Keycloak emailVerified sync failed (user is verified in app DB): {}", e.getMessage());
        }

        safeAudit(user.getId(), "EMAIL_VERIFIED", "Email verified successfully");

        log.info("Email verified for user: {}", user.getEmail());
    }

    /**
     * Issues a fresh token and sends a new email (outside the DB transaction that updates the token).
     */
    public void resendVerificationEmail(String email) {
        final String normalizedEmail = normalizeEmail(email);
        log.info("Resending verification email to {}", normalizedEmail);

        User saved = transactionTemplate.execute(status -> {
            User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new BusinessException("Email is already verified");
            }

            user.issueVerificationToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(EMAIL_VERIFICATION_EXPIRATION_HOURS)
            );
            return userRepository.save(user);
        });

        emailService.sendVerificationEmail(saved.getEmail(), saved.getFirstName(), saved.getVerificationToken());

        safeAudit(saved.getId(), "EMAIL_VERIFICATION_RESENT", "Verification email resent");
    }

    public void logout(UUID userId) {
        log.info("User logout: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found"));

        keycloakAdminClient.revokeUserSessions(user.getKeycloakId());

        safeAudit(userId, "USER_LOGOUT", "User logged out");

        log.info("User logged out successfully");
    }

    private void safeAudit(UUID userId, String action, String description) {
        try {
            auditLogService.logAction(userId, action, description, null, null);
        } catch (Exception e) {
            log.warn("Audit log failed for {} {}: {}", userId, action, e.getMessage());
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
