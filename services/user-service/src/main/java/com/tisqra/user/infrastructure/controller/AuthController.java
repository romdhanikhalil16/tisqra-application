package com.tisqra.user.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.user.application.dto.*;
import com.tisqra.user.application.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authentication REST Controller
 * Handles user registration, login, and password management
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Gmail signup)")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterUserRequest request) {
        UserDTO user = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<UserDTO>builder().success(true).data(user).build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder().success(true).data(response).build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam UUID userId) {
        authenticationService.logout(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestParam String email) {
        authenticationService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/email/verify")
    @Operation(summary = "Verify email address")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/email/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(@RequestParam String email) {
        authenticationService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}
