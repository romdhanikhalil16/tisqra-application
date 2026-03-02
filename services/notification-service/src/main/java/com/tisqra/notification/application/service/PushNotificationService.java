package com.tisqra.notification.application.service;

import com.tisqra.common.enums.NotificationChannel;
import com.tisqra.common.enums.NotificationType;
import com.tisqra.notification.domain.model.Notification;
import com.tisqra.notification.domain.repository.NotificationRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Push notification service using Firebase Cloud Messaging (FCM)
 * Sends push notifications to mobile devices
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final NotificationRepository notificationRepository;

    @Async
    @Transactional
    public void sendPushNotification(
            UUID userId,
            String deviceToken,
            NotificationType type,
            String title,
            String body,
            Map<String, String> data) {
        
        log.info("Sending {} push notification to user: {}", type, userId);

        // Create notification record
        com.tisqra.notification.domain.model.Notification notification = 
            com.tisqra.notification.domain.model.Notification.builder()
                .userId(userId)
                .type(type)
                .channel(NotificationChannel.PUSH)
                .recipient(deviceToken)
                .subject(title)
                .content(body)
                .build();

        try {
            // Build FCM message
            Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putAllData(data != null ? data : Map.of())
                .build();

            // Send via FCM
            String response = FirebaseMessaging.getInstance().send(message);

            // Mark as sent
            notification.markAsSent();
            notificationRepository.save(notification);

            log.info("Push notification sent successfully. Response: {}", response);

        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    public void sendTicketPurchaseNotification(UUID userId, String deviceToken, String eventName) {
        Map<String, String> data = Map.of(
            "type", "TICKET_PURCHASE",
            "eventName", eventName
        );
        sendPushNotification(userId, deviceToken, NotificationType.TICKET_PURCHASE,
            "Ticket Purchase Successful",
            "Your tickets for " + eventName + " have been purchased successfully!",
            data);
    }

    public void sendEventReminderNotification(UUID userId, String deviceToken, String eventName, String eventDate) {
        Map<String, String> data = Map.of(
            "type", "EVENT_REMINDER",
            "eventName", eventName,
            "eventDate", eventDate
        );
        sendPushNotification(userId, deviceToken, NotificationType.EVENT_REMINDER,
            "Event Reminder",
            "Your event " + eventName + " is coming up on " + eventDate,
            data);
    }

    public void sendTicketValidationNotification(UUID userId, String deviceToken, String ticketNumber) {
        Map<String, String> data = Map.of(
            "type", "TICKET_VALIDATION",
            "ticketNumber", ticketNumber
        );
        sendPushNotification(userId, deviceToken, NotificationType.TICKET_VALIDATION,
            "Ticket Validated",
            "Your ticket " + ticketNumber + " has been validated at the entrance",
            data);
    }

    public void sendTicketTransferNotification(UUID userId, String deviceToken, String ticketNumber) {
        Map<String, String> data = Map.of(
            "type", "TICKET_TRANSFER",
            "ticketNumber", ticketNumber
        );
        sendPushNotification(userId, deviceToken, NotificationType.TICKET_TRANSFER,
            "Ticket Received",
            "A ticket has been transferred to you!",
            data);
    }

    public void sendPaymentStatusNotification(UUID userId, String deviceToken, String status, String orderNumber) {
        Map<String, String> data = Map.of(
            "type", "PAYMENT_STATUS",
            "status", status,
            "orderNumber", orderNumber
        );
        sendPushNotification(userId, deviceToken, NotificationType.PAYMENT_STATUS,
            "Payment " + status,
            "Your payment for order " + orderNumber + " is " + status,
            data);
    }
}
