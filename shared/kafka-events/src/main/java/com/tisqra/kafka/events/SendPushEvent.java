package com.tisqra.kafka.events;

import com.tisqra.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendPushEvent {
    private UUID notificationId;
    private UUID userId;
    private String deviceToken;
    private NotificationType type;
    private String title;
    private String body;
    private Map<String, String> data;
    private LocalDateTime createdAt;
    private String eventId;
}

