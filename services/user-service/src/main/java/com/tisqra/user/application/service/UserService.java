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
        validateSuperAdminForRestrictedRoles(request.getRole());

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
        User actor = getCurrentActor();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }
        if (isAdminOrg(actor)) {
            if (!(request.getRole() == UserRole.SCANNER || request.getRole() == UserRole.GUEST)) {
                throw new BusinessException("ADMIN_ORG can only provision SCANNER or GUEST users");
            }
        } else if (!isSuperAdmin(actor)) {
            throw new BusinessException("Only SUPER_ADMIN or ADMIN_ORG can provision users");
        }

        validateSuperAdminForRestrictedRoles(request.getRole());
        UUID organizationId = resolveProvisionOrganizationId(actor, request.getOrganizationId(), request.getRole());

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
            .organizationId(organizationId)
            .role(request.getRole())
            .isActive(true)
            .emailVerified(true)
            .build();

        user = userRepository.save(user);
        auditLogService.logAction(user.getId(), "USER_PROVISIONED",
            "User provisioned by SUPER_ADMIN with role: " + request.getRole(), null, null);

        return userMapper.toDTO(user);
    }

    private void validateSuperAdminForRestrictedRoles(UserRole targetRole) {
        if (targetRole != UserRole.ADMIN_ORG && targetRole != UserRole.SUPER_ADMIN) {
            return;
        }
        User actor = getCurrentActor();
        if (!isSuperAdmin(actor)) {
            throw new BusinessException("Only SUPER_ADMIN can create ADMIN_ORG or SUPER_ADMIN accounts");
        }
    }

    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        User actor = getCurrentActor();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        ensureAdminOrgCanAccessUser(actor, user);
        return userMapper.toDTO(user);
    }

    @Cacheable(value = "users", key = "#email")
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User actor = getCurrentActor();
        User user;
        if (isAdminOrg(actor)) {
            if (actor.getOrganizationId() == null) {
                throw new BusinessException("ADMIN_ORG user is not linked to an organization");
            }
            user = userRepository.findByEmailAndOrganizationId(email, actor.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        } else {
            user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        }
        return userMapper.toDTO(user);
    }

    public UserDTO getUserByKeycloakId(String keycloakId) {
        log.debug("Fetching user by Keycloak ID: {}", keycloakId);
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
        return userMapper.toDTO(user);
    }

    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException("Authentication is required");
        }
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
        return userMapper.toDTO(user);
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        User actor = getCurrentActor();
        if (isSuperAdmin(actor)) {
            return userRepository.findAll(pageable).map(userMapper::toDTO);
        }
        if (isAdminOrg(actor)) {
            if (actor.getOrganizationId() == null) {
                throw new BusinessException("ADMIN_ORG user is not linked to an organization");
            }
            return userRepository.findByOrganizationId(actor.getOrganizationId(), pageable).map(userMapper::toDTO);
        }
        throw new BusinessException("You do not have permission to list users");
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDTO updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        User actor = getCurrentActor();

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        ensureAdminOrgCanManageUser(actor, user);

        userMapper.updateEntityFromDTO(request, user);
        user = userRepository.save(user);

        auditLogService.logAction(user.getId(), "USER_UPDATED", 
            "User profile updated", null, null);

        log.info("User updated successfully: {}", id);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO updateCurrentUser(UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException("Authentication is required");
        }
        User user = userRepository.findByKeycloakId(jwt.getSubject())
            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", jwt.getSubject()));

        userMapper.updateEntityFromDTO(request, user);
        user = userRepository.save(user);

        auditLogService.logAction(user.getId(), "USER_UPDATED", "User profile updated", null, null);
        return userMapper.toDTO(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);
        User actor = getCurrentActor();

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        ensureAdminOrgCanManageUser(actor, user);
        ensureLastSuperAdminSafety(user, false);

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
        User actor = getCurrentActor();

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        ensureAdminOrgCanManageUser(actor, user);

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
        User actor = getCurrentActor();
        if (!isSuperAdmin(actor)) {
            throw new BusinessException("Only SUPER_ADMIN can permanently delete users");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        ensureLastSuperAdminSafety(user, true);

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

    private UUID resolveProvisionOrganizationId(User actor, UUID requestedOrganizationId, UserRole targetRole) {
        if (isAdminOrg(actor)) {
            if (actor.getOrganizationId() == null) {
                throw new BusinessException("ADMIN_ORG user is not linked to an organization");
            }
            return actor.getOrganizationId();
        }
        if (targetRole == UserRole.GUEST) {
            return requestedOrganizationId;
        }
        if ((targetRole == UserRole.SCANNER || targetRole == UserRole.ADMIN_ORG) && requestedOrganizationId == null) {
            throw new BusinessException("organizationId is required for SCANNER and ADMIN_ORG provisioning");
        }
        return requestedOrganizationId;
    }

    private User getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException("Authentication is required");
        }
        return userRepository.findByKeycloakId(jwt.getSubject())
            .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", jwt.getSubject()));
    }

    private boolean isSuperAdmin(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    private boolean isAdminOrg(User user) {
        return user.getRole() == UserRole.ADMIN_ORG;
    }

    private void ensureAdminOrgCanAccessUser(User actor, User target) {
        if (!isAdminOrg(actor)) {
            return;
        }
        if (actor.getOrganizationId() == null) {
            throw new BusinessException("ADMIN_ORG user is not linked to an organization");
        }
        if (!actor.getOrganizationId().equals(target.getOrganizationId())) {
            throw new ResourceNotFoundException("User", "id", target.getId());
        }
    }

    private void ensureAdminOrgCanManageUser(User actor, User target) {
        if (!isAdminOrg(actor)) {
            return;
        }
        if (actor.getOrganizationId() == null) {
            throw new BusinessException("ADMIN_ORG user is not linked to an organization");
        }
        if (!actor.getOrganizationId().equals(target.getOrganizationId())) {
            throw new ResourceNotFoundException("User", "id", target.getId());
        }
        if (target.getRole() != UserRole.SCANNER && target.getRole() != UserRole.GUEST) {
            throw new BusinessException("ADMIN_ORG can only manage SCANNER and GUEST users");
        }
    }

    private void ensureLastSuperAdminSafety(User target, boolean deleting) {
        if (target.getRole() != UserRole.SUPER_ADMIN || !Boolean.TRUE.equals(target.getIsActive())) {
            return;
        }
        long activeSuperAdmins = userRepository.countByRoleAndIsActiveTrue(UserRole.SUPER_ADMIN);
        if (activeSuperAdmins <= 1) {
            throw new BusinessException(deleting
                ? "Cannot delete the only active SUPER_ADMIN"
                : "Cannot deactivate the only active SUPER_ADMIN");
        }
    }
}
