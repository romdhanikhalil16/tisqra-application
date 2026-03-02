package com.tisqra.notification.application.service;

import com.tisqra.common.enums.NotificationChannel;
import com.tisqra.common.enums.NotificationType;
import com.tisqra.notification.domain.model.Notification;
import com.tisqra.notification.domain.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.UUID;

/**
 * Email service using Brevo SMTP
 * Sends transactional emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    @Value("${brevo.sender.email:noreply@eventticket.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:Event Ticketing Platform}")
    private String senderName;

    @Async
    @Transactional
    public void sendEmail(
            UUID userId,
            String recipientEmail,
            String recipientName,
            NotificationType type,
            String subject,
            String templateName,
            Map<String, Object> templateData) {
        
        log.info("Sending {} email to: {}", type, recipientEmail);

        // Create notification record
        Notification notification = Notification.builder()
            .userId(userId)
            .type(type)
            .channel(NotificationChannel.EMAIL)
            .recipient(recipientEmail)
            .subject(subject)
            .build();

        try {
            // Process email template
            Context context = new Context();
            context.setVariables(templateData);
            context.setVariable("recipientName", recipientName);
            String htmlContent = templateEngine.process(templateName, context);

            // Send email via Brevo SMTP
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            // Mark as sent
            notification.setContent(htmlContent);
            notification.markAsSent();
            notificationRepository.save(notification);

            log.info("Email sent successfully to: {}", recipientEmail);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", recipientEmail, e);
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    public void sendAccountVerificationEmail(String email, String name, String verificationToken) {
        Map<String, Object> data = Map.of(
            "verificationLink", "http://localhost:8080/api/auth/email/verify?token=" + verificationToken
        );
        sendEmail(null, email, name, NotificationType.ACCOUNT_VERIFICATION,
            "Verify your account", "email/account-verification", data);
    }

    public void sendTicketPurchaseEmail(UUID userId, String email, String name, Map<String, Object> orderData) {
        sendEmail(userId, email, name, NotificationType.TICKET_PURCHASE,
            "Your ticket purchase confirmation", "email/ticket-purchase", orderData);
    }

    public void sendPasswordResetEmail(String email, String name, String resetToken) {
        Map<String, Object> data = Map.of(
            "resetLink", "http://localhost:8080/reset-password?token=" + resetToken
        );
        sendEmail(null, email, name, NotificationType.PASSWORD_RESET,
            "Reset your password", "email/password-reset", data);
    }

    public void sendTicketTransferEmail(UUID userId, String email, String name, Map<String, Object> ticketData) {
        sendEmail(userId, email, name, NotificationType.TICKET_TRANSFER,
            "Ticket transferred to you", "email/ticket-transfer", ticketData);
    }

    public void sendPaymentReceiptEmail(UUID userId, String email, String name, Map<String, Object> paymentData) {
        sendEmail(userId, email, name, NotificationType.PAYMENT_RECEIPT,
            "Payment receipt", "email/payment-receipt", paymentData);
    }
}
