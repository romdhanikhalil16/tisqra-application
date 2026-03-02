package com.tisqra.notification.domain.repository;

import com.tisqra.common.enums.NotificationType;
import com.tisqra.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Notification repository interface
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Long countByUserIdAndReadFalse(UUID userId);
}
