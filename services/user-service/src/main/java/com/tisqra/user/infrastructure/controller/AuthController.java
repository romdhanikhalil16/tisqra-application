package com.tisqra.user.infrastructure.controller;

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
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterUserRequest request) {
        UserDTO user = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<Void> logout(@RequestParam UUID userId) {
        authenticationService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Void> requestPasswordReset(@RequestParam String email) {
        authenticationService.requestPasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify")
    @Operation(summary = "Verify email address")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }
}
