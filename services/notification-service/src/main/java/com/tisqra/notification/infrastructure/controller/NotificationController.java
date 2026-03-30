package com.tisqra.notification.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.notification.application.dto.NotificationDTO;
import com.tisqra.notification.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUserNotifications(
        @PathVariable UUID userId,
        Pageable pageable) {

        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.status(OK)
            .body(ApiResponse.<Page<NotificationDTO>>builder()
                .success(true)
                .data(notifications)
                .build());
    }

    @PostMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.status(OK)
            .body(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID notificationId) {
        notificationService.delete(notificationId);
        return ResponseEntity.status(OK)
            .body(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}

