package com.tisqra.user.infrastructure.keycloak;

import com.tisqra.common.enums.UserRole;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.user.application.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Keycloak Admin Client for user management
 * Handles user creation, authentication, and management in Keycloak
 */
@Slf4j
@Component
public class KeycloakAdminClient {

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:event-ticketing}")
    private String realm;

    @Value("${keycloak.client-id:event-ticketing-client}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Create a new user in Keycloak
     */
    public String createUser(String email, String password, String firstName, String lastName, UserRole role) {
        log.info("Creating user in Keycloak: {}", email);

        try {
            // Get admin access token
            String adminToken = getAdminToken();

            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", email);
            userData.put("email", email);
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("enabled", true);
            userData.put("emailVerified", false);

            // Set credentials
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", password);
            credential.put("temporary", false);
            userData.put("credentials", Collections.singletonList(credential));

            // Set role
            userData.put("realmRoles", Collections.singletonList(role.name()));

            // Create user
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userData, headers);

            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // Extract user ID from Location header
            String location = response.getHeaders().getLocation().toString();
            String userId = location.substring(location.lastIndexOf('/') + 1);

            log.info("User created in Keycloak with ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("Failed to create user in Keycloak", e);
            throw new BusinessException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Authenticate user and get tokens
     */
    public LoginResponse authenticate(String email, String password) {
        log.info("Authenticating user: {}", email);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", email);
            body.add("password", password);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> tokenData = response.getBody();

            return LoginResponse.builder()
                .accessToken((String) tokenData.get("access_token"))
                .refreshToken((String) tokenData.get("refresh_token"))
                .tokenType((String) tokenData.get("token_type"))
                .expiresIn(((Number) tokenData.get("expires_in")).longValue())
                .build();

        } catch (Exception e) {
            log.error("Authentication failed", e);
            throw new BusinessException("Invalid credentials");
        }
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String keycloakId) {
        log.info("Generating password reset token for user: {}", keycloakId);
        // In production, this would trigger Keycloak's password reset flow
        // For now, return a mock token
        return UUID.randomUUID().toString();
    }

    /**
     * Reset user password
     */
    public String resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        // In production, validate token and reset password in Keycloak
        // For now, return mock keycloak ID
        return UUID.randomUUID().toString();
    }

    /**
     * Verify user email
     */
    public String verifyEmail(String verificationToken) {
        log.info("Verifying email with token");
        // In production, validate token and mark email as verified in Keycloak
        return UUID.randomUUID().toString();
    }

    /**
     * Revoke all user sessions
     */
    public void revokeUserSessions(String keycloakId) {
        log.info("Revoking sessions for user: {}", keycloakId);
        // In production, revoke all sessions in Keycloak
    }

    /**
     * Get admin access token
     */
    private String getAdminToken() {
        // In production, get admin token from Keycloak
        // For now, return mock token
        return "mock-admin-token";
    }
}
