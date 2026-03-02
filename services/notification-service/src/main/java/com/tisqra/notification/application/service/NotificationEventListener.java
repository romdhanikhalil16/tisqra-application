package com.tisqra.notification.application.service;

import com.tisqra.kafka.events.SendEmailEvent;
import com.tisqra.kafka.events.SendPushEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka event listener for notification events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;

    @KafkaListener(topics = "notification.email.send", groupId = "notification-service")
    public void handleSendEmailEvent(SendEmailEvent event) {
        log.info("Received email notification event: {}", event.getNotificationId());
        
        emailService.sendEmail(
            null,
            event.getRecipientEmail(),
            event.getRecipientName(),
            event.getType(),
            event.getSubject(),
            event.getTemplateName(),
            event.getTemplateData()
        );
    }

    @KafkaListener(topics = "notification.push.send", groupId = "notification-service")
    public void handleSendPushEvent(SendPushEvent event) {
        log.info("Received push notification event: {}", event.getNotificationId());
        
        pushNotificationService.sendPushNotification(
            event.getUserId(),
            event.getDeviceToken(),
            event.getType(),
            event.getTitle(),
            event.getBody(),
            event.getData()
        );
    }
}
