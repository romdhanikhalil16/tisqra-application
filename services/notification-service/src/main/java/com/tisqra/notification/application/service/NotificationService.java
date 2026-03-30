package com.tisqra.notification.application.service;

import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.notification.application.dto.NotificationDTO;
import com.tisqra.notification.domain.model.Notification;
import com.tisqra.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
            .map(this::toDTO);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public void delete(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .type(notification.getType())
            .channel(notification.getChannel())
            .recipient(notification.getRecipient())
            .subject(notification.getSubject())
            .content(notification.getContent())
            .sent(notification.getSent())
            .read(notification.getRead())
            .sentAt(notification.getSentAt())
            .errorMessage(notification.getErrorMessage())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}

