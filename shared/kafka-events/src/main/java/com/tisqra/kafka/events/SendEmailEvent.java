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
public class SendEmailEvent {
    private UUID notificationId;
    private String recipientEmail;
    private String recipientName;
    private NotificationType type;
    private String subject;
    private String templateName;
    private Map<String, Object> templateData;
    private LocalDateTime createdAt;
    private String eventId;
}

