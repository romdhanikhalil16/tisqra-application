package com.tisqra.user.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.user.application.dto.CreateUserRequest;
import com.tisqra.user.application.dto.UpdateUserRequest;
import com.tisqra.user.application.dto.UserDTO;
import com.tisqra.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User REST Controller
 * Handles HTTP requests for user management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<UserDTO>builder().success(true).data(user).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG') or #id == authentication.principal.claims['sub']")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserDTO>builder().success(true).data(user).build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.<UserDTO>builder().success(true).data(user).build());
    }

    @GetMapping("/keycloak/{keycloakId}")
    @Operation(summary = "Get user by Keycloak ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByKeycloakId(@PathVariable String keycloakId) {
        UserDTO user = userService.getUserByKeycloakId(keycloakId);
        return ResponseEntity.ok(ApiResponse.<UserDTO>builder().success(true).data(user).build());
    }

    @GetMapping
    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<UserDTO>>builder().success(true).data(users).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG') or #id == authentication.principal.claims['sub']")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.<UserDTO>builder().success(true).data(user).build());
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user account")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate user account")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/{id}/verify-email")
    @Operation(summary = "Verify user email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable UUID id) {
        userService.verifyEmail(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/{id}/login")
    @Operation(summary = "Record user login")
    public ResponseEntity<ApiResponse<Void>> recordLogin(@PathVariable UUID id) {
        userService.recordLogin(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}
