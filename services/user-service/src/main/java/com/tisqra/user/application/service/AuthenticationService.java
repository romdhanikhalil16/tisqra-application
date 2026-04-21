package com.tisqra.user.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Authentication service - Handles user registration, login, and password management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final long EMAIL_VERIFICATION_EXPIRATION_HOURS = 24L;
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_UPPER_BOUND = 1_000_000;
    private static final int VERIFICATION_CODE_GENERATION_MAX_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

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
        if (role != UserRole.GUEST) {
            throw new BusinessException("Public registration is only allowed for GUEST accounts");
        }

        final String username = Optional.ofNullable(request.getUsername())
            .map(String::trim)
            .filter(u -> !u.isEmpty())
            .orElse(normalizedEmail);

        String keycloakId;
        try {
            keycloakId = keycloakAdminClient.createUser(
                username,
                normalizedEmail,
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                role
            );
        } catch (BusinessException ex) {
            // If Postgres was manually modified (row deleted) but Keycloak still has the user,
            // make registration idempotent by re-linking to the existing Keycloak account.
            // Security: require the caller to prove they know the existing password.
            String msg = String.valueOf(ex.getMessage());
            boolean looksLikeKeycloakConflict = msg.contains("409") || msg.toLowerCase().contains("user exists");
            if (!looksLikeKeycloakConflict) {
                throw ex;
            }

            keycloakAdminClient.authenticate(normalizedEmail, request.getPassword());
            keycloakId = keycloakAdminClient.findUserIdByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException(
                    "User exists in Keycloak but could not be resolved by email; contact support"
                ));
        }

        final String resolvedKeycloakId = keycloakId;
        User saved = transactionTemplate.execute(status -> {
            User user = User.builder()
                .email(normalizedEmail)
                .keycloakId(resolvedKeycloakId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .emailVerified(false)
                .build();
            user.issueVerificationToken(
                generateUniqueVerificationCode(),
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

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseGet(() -> provisionLocalUserFromCredentials(normalizedEmail, request.getPassword()));

        if (!user.getIsActive()) {
            throw new BusinessException("User account is deactivated");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Please verify your email before logging in");
        }

        LoginResponse loginResponse = keycloakAdminClient.authenticate(
            normalizedEmail,
            request.getPassword()
        );
        log.debug("Token generated for {} with token_type={}", normalizedEmail, loginResponse.getTokenType());

        user.updateLastLogin();
        userRepository.save(user);

        loginResponse.setUser(userMapper.toDTO(user));

        safeAudit(user.getId(), "USER_LOGIN", "User logged in successfully");

        log.info("User logged in successfully: {}", normalizedEmail);
        return loginResponse;
    }

    @Transactional
    public LoginResponse registerAndIssueToken(RegisterUserRequest request) {
        UserDTO user = register(request);
        LoginResponse response = null;
        BusinessException lastAuthError = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                response = keycloakAdminClient.authenticate(user.getEmail(), request.getPassword());
                break;
            } catch (BusinessException ex) {
                lastAuthError = ex;
                log.warn("Register token issue attempt {} failed for {}", attempt, user.getEmail());
                try {
                    Thread.sleep(350L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (response == null) {
            throw lastAuthError != null ? lastAuthError : new BusinessException("Failed to issue access token after registration");
        }
        response.setUser(user);
        log.debug("Issued register token for {} with token_type={}", user.getEmail(), response.getTokenType());
        return response;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        final String normalizedEmail = normalizeEmail(email);
        log.info("Password reset requested for email: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

        String resetToken = keycloakAdminClient.generatePasswordResetToken(user.getKeycloakId());
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);

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
                generateUniqueVerificationCode(),
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

    private String generateUniqueVerificationCode() {
        for (int attempt = 1; attempt <= VERIFICATION_CODE_GENERATION_MAX_ATTEMPTS; attempt++) {
            String candidate = String.format(
                "%0" + VERIFICATION_CODE_LENGTH + "d",
                ThreadLocalRandom.current().nextInt(0, VERIFICATION_CODE_UPPER_BOUND)
            );
            if (!userRepository.existsByVerificationToken(candidate)) {
                return candidate;
            }
        }
        throw new BusinessException("Failed to generate verification code, please try again");
    }

    @Transactional
    protected User provisionLocalUserFromAccessToken(String accessToken, String fallbackEmail) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                throw new BusinessException("Invalid credentials");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(payloadBytes, new TypeReference<>() {});

            String email = normalizeEmail((String) claims.getOrDefault("email", fallbackEmail));
            if (email == null || email.isBlank()) {
                throw new BusinessException("Invalid credentials");
            }

            String keycloakId = (String) claims.get("sub");
            if (keycloakId == null || keycloakId.isBlank()) {
                throw new BusinessException("Invalid credentials");
            }

            UserRole role = resolveRoleFromClaims(claims);
            String firstName = (String) claims.getOrDefault("given_name", "User");
            String lastName = (String) claims.getOrDefault("family_name", "Account");
            boolean emailVerified = Boolean.TRUE.equals(claims.get("email_verified"));

            User user = User.builder()
                .email(email)
                .keycloakId(keycloakId)
                .firstName(firstName)
                .lastName(lastName)
                .phone(null)
                .role(role)
                .isActive(true)
                .emailVerified(emailVerified)
                .build();

            return userRepository.save(user);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to provision local user from access token", ex);
            throw new BusinessException("Invalid credentials");
        }
    }

    @Transactional
    protected User provisionLocalUserFromCredentials(String email, String password) {
        LoginResponse response = keycloakAdminClient.authenticate(email, password);
        User user = provisionLocalUserFromAccessToken(response.getAccessToken(), email);
        log.info("Provisioned local user from Keycloak login for {}", email);
        return user;
    }

    @SuppressWarnings("unchecked")
    private UserRole resolveRoleFromClaims(Map<String, Object> claims) {
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object rolesObj = realmAccessMap.get("roles");
            if (rolesObj instanceof List<?> roles) {
                if (roles.contains("SUPER_ADMIN")) {
                    return UserRole.SUPER_ADMIN;
                }
                if (roles.contains("ADMIN_ORG")) {
                    return UserRole.ADMIN_ORG;
                }
                if (roles.contains("SCANNER")) {
                    return UserRole.SCANNER;
                }
            }
        }
        return UserRole.GUEST;
    }
}
