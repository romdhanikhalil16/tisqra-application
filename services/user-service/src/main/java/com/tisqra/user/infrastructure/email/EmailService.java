package com.tisqra.user.infrastructure.email;

import com.tisqra.common.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails required by user-service (real SMTP, no mocks).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.email-verification.base-url:http://localhost:8080/api/auth/email/verify?token=}")
    private String verificationBaseUrl;

    @Value("${app.password-reset.base-url:http://localhost:8080/reset-password?token=}")
    private String passwordResetBaseUrl;

    public void sendVerificationEmail(String toEmail, String firstName, String verificationToken) {
        final String verificationLink = verificationBaseUrl + verificationToken;
        final String displayName = firstName == null || firstName.trim().isEmpty() ? "User" : firstName.trim();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(toEmail);
            helper.setSubject("Verify your TISQRA account");
            helper.setText(
                "Hi " + displayName + ",\n\n"
                    + "Welcome to TISQRA. Please verify your email by opening the link below:\n"
                    + verificationLink + "\n\n"
                    + "This link expires in 24 hours.\n\n"
                    + "If you did not create this account, please ignore this email.\n",
                false
            );
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}", toEmail, ex);
            throw new BusinessException("Failed to send verification email: " + ex.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        final String resetLink = passwordResetBaseUrl + resetToken;
        final String displayName = firstName == null || firstName.trim().isEmpty() ? "User" : firstName.trim();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(toEmail);
            helper.setSubject("Reset your TISQRA password");
            helper.setText(
                "Hi " + displayName + ",\n\n"
                    + "Use the link below to reset your password:\n"
                    + resetLink + "\n\n"
                    + "If you did not request a password reset, ignore this email.\n",
                false
            );
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}", toEmail, ex);
            throw new BusinessException("Failed to send password reset email: " + ex.getMessage());
        }
    }
}
