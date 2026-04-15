package com.tisqra.user.application.service;

import com.tisqra.common.enums.UserRole;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.user.application.dto.CreateUserRequest;
import com.tisqra.user.application.dto.ProvisionUserRequest;
import com.tisqra.user.application.dto.ResetRegisteredUsersResponse;
import com.tisqra.user.application.dto.UpdateUserRequest;
import com.tisqra.user.application.dto.UserDTO;
import com.tisqra.user.application.mapper.UserMapper;
import com.tisqra.user.domain.model.AuditLog;
import com.tisqra.user.domain.model.User;
import com.tisqra.user.domain.repository.AuditLogRepository;
import com.tisqra.user.domain.repository.UserRepository;
import com.tisqra.user.infrastructure.keycloak.KeycloakAdminClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * User service - Application layer
 * Handles user management business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final KeycloakAdminClient keycloakAdminClient;
    private final CacheManager cacheManager;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        validateAdminOrgCreationAccess(request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByKeycloakId(request.getKeycloakId())) {
            throw new BusinessException("User with Keycloak ID already exists");
        }

        User user = userMapper.toEntity(request);
        user = userRepository.save(user);

        auditLogService.logAction(user.getId(), "USER_CREATED", 
            "User created with email: " + user.getEmail(), null, null);

        log.info("User created successfully with ID: {}", user.getId());
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO provisionUser(ProvisionUserRequest request) {
        log.info("Provisioning managed user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }
        if (request.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Provisioning SUPER_ADMIN via API is not allowed");
        }

        String keycloakId = keycloakAdminClient.createUser(
            request.getEmail(),
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            request.getRole()
        );

        User user = User.builder()
            .email(request.getEmail())
            .keycloakId(keycloakId)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .role(request.getRole())
            .isActive(true)
            .emailVerified(true)
            .build();

        user = userRepository.save(user);
        auditLogService.logAction(user.getId(), "USER_PROVISIONED",
            "User provisioned by SUPER_ADMIN with role: " + request.getRole(), null, null);

        return userMapper.toDTO(user);
    }

    private void validateAdminOrgCreationAccess(UserRole targetRole) {
        if (targetRole != UserRole.ADMIN_ORG) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException("Only SUPER_ADMIN can create ADMIN_ORG accounts");
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Object rolesObj = realmAccess == null ? null : realmAccess.get("roles");
        Collection<?> roles = rolesObj instanceof Collection<?> c ? c : Collections.emptyList();
        boolean isSuperAdmin = roles.stream().anyMatch(role -> "SUPER_ADMIN".equals(String.valueOf(role)));

        if (!isSuperAdmin) {
            throw new BusinessException("Only SUPER_ADMIN can create ADMIN_ORG accounts");
        }
    }

    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }

    @Cacheable(value = "users", key = "#email")
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDTO(user);
    }

    public UserDTO getUserByKeycloakId(String keycloakId) {
        log.debug("Fetching user by Keycloak ID: {}", keycloakId);
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
        return userMapper.toDTO(user);
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDTO updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userMapper.updateEntityFromDTO(request, user);
        user = userRepository.save(user);

        auditLogService.logAction(user.getId(), "USER_UPDATED", 
            "User profile updated", null, null);

        log.info("User updated successfully: {}", id);
        return userMapper.toDTO(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.deactivate();
        userRepository.save(user);

        auditLogService.logAction(user.getId(), "USER_DEACTIVATED", 
            "User account deactivated", null, null);

        log.info("User deactivated successfully: {}", id);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void activateUser(UUID id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.activate();
        userRepository.save(user);

        auditLogService.logAction(user.getId(), "USER_ACTIVATED", 
            "User account activated", null, null);

        log.info("User activated successfully: {}", id);
    }

    @Transactional
    public void recordLogin(UUID userId) {
        log.debug("Recording login for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.updateLastLogin();
        userRepository.save(user);

        auditLogService.logAction(userId, "USER_LOGIN", "User logged in", null, null);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void verifyEmail(UUID id) {
        log.info("Verifying email for user: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.verifyEmail();
        userRepository.save(user);

        auditLogService.logAction(user.getId(), "EMAIL_VERIFIED", 
            "Email verified successfully", null, null);

        log.info("Email verified for user: {}", id);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void permanentlyDeleteUser(UUID id) {
        log.info("Permanently deleting user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        keycloakAdminClient.deleteUser(user.getKeycloakId());
        userRepository.delete(user);

        auditLogService.logAction(id, "USER_DELETED_PERMANENTLY",
            "User permanently deleted from application database and Keycloak", null, null);

        log.info("User permanently deleted: {}", id);
    }

    @Transactional
    public ResetRegisteredUsersResponse resetRegisteredUsersPreservingSuperAdmin() {
        int deleted = 0;
        int skipped = 0;
        for (AuditLog auditLog : auditLogRepository.findByAction("USER_REGISTERED")) {
            User user = userRepository.findById(auditLog.getUserId()).orElse(null);
            if (user == null) {
                continue;
            }
            boolean isSuperAdmin = user.getRole() == UserRole.SUPER_ADMIN
                || "admin@eventticketing.com".equalsIgnoreCase(user.getEmail());
            if (isSuperAdmin) {
                skipped++;
                continue;
            }

            keycloakAdminClient.deleteUser(user.getKeycloakId());
            auditLogRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
            deleted++;
        }

        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").clear();
        }

        log.info("Reset registered users completed: deleted={}, skipped={}", deleted, skipped);
        return ResetRegisteredUsersResponse.builder()
            .deletedCount(deleted)
            .skippedCount(skipped)
            .build();
    }
}
