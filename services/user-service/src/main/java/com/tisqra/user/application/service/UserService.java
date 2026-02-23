package com.tisqra.user.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.user.application.dto.CreateUserRequest;
import com.tisqra.user.application.dto.UpdateUserRequest;
import com.tisqra.user.application.dto.UserDTO;
import com.tisqra.user.application.mapper.UserMapper;
import com.tisqra.user.domain.model.User;
import com.tisqra.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

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
}
