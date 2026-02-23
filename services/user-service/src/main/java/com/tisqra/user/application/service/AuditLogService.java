package com.tisqra.user.application.service;

import com.tisqra.user.domain.model.AuditLog;
import com.tisqra.user.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Audit log service - Application layer
 * Handles audit logging for security and compliance
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logAction(UUID userId, String action, String description, String ipAddress, String userAgent) {
        log.debug("Logging audit action: {} for user: {}", action, userId);

        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .action(action)
            .description(description)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogs(UUID userId, Pageable pageable) {
        log.debug("Fetching audit logs for user: {}", userId);
        return auditLogRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getActionAuditLogs(String action, Pageable pageable) {
        log.debug("Fetching audit logs for action: {}", action);
        return auditLogRepository.findByAction(action, pageable);
    }
}
