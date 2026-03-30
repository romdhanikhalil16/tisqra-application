package com.tisqra.notification.application.dto;

import com.tisqra.common.enums.NotificationChannel;
import com.tisqra.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private NotificationType type;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String content;
    private Boolean sent;
    private Boolean read;
    private LocalDateTime sentAt;
    private String errorMessage;
    private LocalDateTime createdAt;
}

