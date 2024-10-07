package com.anazih.ptmback.auditlog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void createAuditLog(String eventType, String entityType, UUID entityId, UUID userId, String description) {
        log.info("Creating audit log with eventType: {}, entityType: {}, entityId: {}, userId: {}, description: {}", eventType, entityType, entityId, userId, description);
        auditLogRepository.save(AuditLog.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .description(description)
                .build());
    }

    public Page<AuditLog> getAuditLogs(
            String eventType,
            String entityType,
            UUID entityId,
            UUID userId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable) {
        log.info("Fetching audit logs");
        return auditLogRepository.findFilteredLogs(eventType, entityType, entityId, userId, fromDate, toDate, pageable);

    }
}
