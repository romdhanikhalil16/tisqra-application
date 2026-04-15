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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, PasswordResetTokenEntry> passwordResetTokens = new ConcurrentHashMap<>();

    public Optional<String> findUserIdByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            String adminToken = getAdminToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users?email=" + email.trim();
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
            );

            List<?> body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            for (Object item : body) {
                if (!(item instanceof Map<?, ?> m)) {
                    continue;
                }
                Object id = m.get("id");
                Object kcEmail = m.get("email");
                if (id != null && kcEmail != null && email.trim().equalsIgnoreCase(String.valueOf(kcEmail))) {
                    return Optional.of(String.valueOf(id));
                }
            }
            for (Object item : body) {
                if (item instanceof Map<?, ?> m) {
                    Object id = m.get("id");
                    if (id != null) {
                        return Optional.of(String.valueOf(id));
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            log.warn("Keycloak lookup by email failed for {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Create a new user in Keycloak
     */
    public String createUser(String username, String email, String password, String firstName, String lastName, UserRole role) {
        log.info("Creating user in Keycloak: {}", email);

        try {
            // Get admin access token
            String adminToken = getAdminToken();

            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("enabled", true);
            userData.put("emailVerified", true);

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

        } catch (HttpStatusCodeException e) {
            // Preserve status text in message so caller can handle conflicts (409) explicitly.
            log.error("Failed to create user in Keycloak (status {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Failed to create user: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
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
            // Only include client_secret when configured.
            // Many Keycloak setups use a public client for direct-access-grants.
            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                body.add("client_secret", clientSecret);
            }
            body.add("username", email);
            body.add("password", password);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> tokenData = response.getBody();

            LoginResponse responsePayload = LoginResponse.builder()
                .accessToken((String) tokenData.get("access_token"))
                .refreshToken((String) tokenData.get("refresh_token"))
                .tokenType((String) tokenData.get("token_type"))
                .expiresIn(((Number) tokenData.get("expires_in")).longValue())
                .build();
            log.debug("Keycloak token generated for {} with tokenType={} expiresIn={}", email, responsePayload.getTokenType(), responsePayload.getExpiresIn());
            return responsePayload;

        } catch (HttpStatusCodeException e) {
            log.error("Authentication failed with status {} and body {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 400) {
                try {
                    selfHealRealmAndClientSettings();
                    return authenticateWithoutSelfHeal(email, password);
                } catch (Exception retryEx) {
                    log.error("Authentication retry after self-heal failed", retryEx);
                }
            }
            throw new BusinessException("Invalid credentials");
        } catch (Exception e) {
            log.error("Authentication failed", e);
            throw new BusinessException("Invalid credentials");
        }
    }

    private LoginResponse authenticateWithoutSelfHeal(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.trim().isEmpty()) {
            body.add("client_secret", clientSecret);
        }
        body.add("username", email);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> tokenData = response.getBody();
        LoginResponse responsePayload = LoginResponse.builder()
            .accessToken((String) tokenData.get("access_token"))
            .refreshToken((String) tokenData.get("refresh_token"))
            .tokenType((String) tokenData.get("token_type"))
            .expiresIn(((Number) tokenData.get("expires_in")).longValue())
            .build();
        log.debug("Keycloak token generated for {} with tokenType={} expiresIn={}", email, responsePayload.getTokenType(), responsePayload.getExpiresIn());
        return responsePayload;
    }

    @SuppressWarnings("unchecked")
    private void selfHealRealmAndClientSettings() {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // Ensure realm accepts email-based login.
        String realmUrl = keycloakServerUrl + "/admin/realms/" + realm;
        ResponseEntity<Map> realmResp = restTemplate.exchange(realmUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> realmData = realmResp.getBody();
        if (realmData != null && !Boolean.TRUE.equals(realmData.get("loginWithEmailAllowed"))) {
            realmData.put("loginWithEmailAllowed", true);
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setContentType(MediaType.APPLICATION_JSON);
            putHeaders.setBearerAuth(adminToken);
            restTemplate.exchange(realmUrl, HttpMethod.PUT, new HttpEntity<>(realmData, putHeaders), Void.class);
            log.warn("Updated Keycloak realm setting loginWithEmailAllowed=true");
        }

        // Ensure configured client has direct access grants enabled.
        String clientsUrl = keycloakServerUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId;
        ResponseEntity<List> clientsResp = restTemplate.exchange(clientsUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        List<?> clients = clientsResp.getBody();
        if (clients == null || clients.isEmpty()) {
            return;
        }
        Object first = clients.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return;
        }
        Object idObj = firstMap.get("id");
        if (idObj == null) {
            return;
        }
        String internalClientId = String.valueOf(idObj);
        String clientUrl = keycloakServerUrl + "/admin/realms/" + realm + "/clients/" + internalClientId;
        ResponseEntity<Map> clientResp = restTemplate.exchange(clientUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> clientData = clientResp.getBody();
        if (clientData != null) {
            boolean needsUpdate = !Boolean.TRUE.equals(clientData.get("directAccessGrantsEnabled"))
                || !Boolean.TRUE.equals(clientData.get("publicClient"))
                || Boolean.TRUE.equals(clientData.get("bearerOnly"));
            if (!needsUpdate) {
                return;
            }
            clientData.put("directAccessGrantsEnabled", true);
            clientData.put("publicClient", true);
            clientData.put("bearerOnly", false);
            clientData.put("standardFlowEnabled", true);
            clientData.put("serviceAccountsEnabled", false);
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setContentType(MediaType.APPLICATION_JSON);
            putHeaders.setBearerAuth(adminToken);
            restTemplate.exchange(clientUrl, HttpMethod.PUT, new HttpEntity<>(clientData, putHeaders), Void.class);
            log.warn("Updated Keycloak client {} settings for public direct-access-grants", clientId);
        }
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String keycloakId) {
        log.info("Generating password reset token for user: {}", keycloakId);
        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(token, new PasswordResetTokenEntry(keycloakId, LocalDateTime.now().plusMinutes(30)));
        log.debug("Generated password reset token for user {}", keycloakId);
        return token;
    }

    /**
     * Reset user password
     */
    public String resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        PasswordResetTokenEntry entry = passwordResetTokens.get(token);
        if (entry == null) {
            throw new BusinessException("Invalid or expired reset token");
        }
        if (entry.expiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokens.remove(token);
            throw new BusinessException("Invalid or expired reset token");
        }

        try {
            String adminToken = getAdminToken();
            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + entry.keycloakUserId() + "/reset-password";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "password");
            payload.put("temporary", false);
            payload.put("value", newPassword);

            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(payload, headers), Void.class);
            passwordResetTokens.remove(token);
            return entry.keycloakUserId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to reset password in Keycloak", e);
            throw new BusinessException("Failed to reset password");
        }
    }

    /**
     * Revoke all user sessions (invalidates access tokens server-side).
     */
    public void revokeUserSessions(String keycloakId) {
        log.info("Revoking sessions for user: {}", keycloakId);
        try {
            String adminToken = getAdminToken();
            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakId + "/logout";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.postForEntity(url, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            log.warn("Keycloak session revoke returned {} for user {}: {}", e.getStatusCode(), keycloakId, e.getResponseBodyAsString());
        } catch (Exception e) {
            log.warn("Keycloak session revoke failed for user {}: {}", keycloakId, e.getMessage());
        }
    }

    /**
     * Mark email as verified in Keycloak (keeps IdP in sync with application DB).
     */
    public void markEmailVerified(String keycloakUserId) {
        try {
            String adminToken = getAdminToken();
            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;
            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(adminToken);
            ResponseEntity<Map> getResp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders),
                Map.class
            );
            Map<String, Object> body = getResp.getBody();
            if (body == null) {
                throw new BusinessException("Keycloak user not found");
            }
            body.put("emailVerified", true);

            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setContentType(MediaType.APPLICATION_JSON);
            putHeaders.setBearerAuth(adminToken);
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, putHeaders), Void.class);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to mark email verified in Keycloak for {}", keycloakUserId, e);
            throw new BusinessException("Failed to sync email verification with Keycloak: " + e.getMessage());
        }
    }

    /**
     * Permanently delete a user from Keycloak by id.
     */
    public void deleteUser(String keycloakUserId) {
        log.info("Deleting user from Keycloak: {}", keycloakUserId);
        try {
            String adminToken = getAdminToken();
            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            log.error("Failed to delete Keycloak user {} (status {}): {}", keycloakUserId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Failed to delete user from Keycloak: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to delete Keycloak user {}", keycloakUserId, e);
            throw new BusinessException("Failed to delete user from Keycloak: " + e.getMessage());
        }
    }

    /**
     * Get admin access token
     */
    private String getAdminToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", "admin-cli");
            body.add("username", adminUsername);
            body.add("password", adminPassword);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String url = keycloakServerUrl + "/realms/master/protocol/openid-connect/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> tokenData = response.getBody();

            if (tokenData == null || tokenData.get("access_token") == null) {
                throw new BusinessException("Keycloak admin token response missing access_token");
            }

            return tokenData.get("access_token").toString();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get Keycloak admin token", e);
            throw new BusinessException("Failed to get Keycloak admin token: " + e.getMessage());
        }
    }

    private record PasswordResetTokenEntry(String keycloakUserId, LocalDateTime expiresAt) {}
}
