package com.anazih.ptmback.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    @Query("SELECT a FROM AuditLog a " +
            "WHERE (:eventType IS NULL OR a.eventType = :eventType) " +
            "AND (:entityType IS NULL OR a.entityType = :entityType) " +
            "AND (:entityId IS NULL OR a.entityId = :entityId) " +
            "AND (:userId IS NULL OR a.userId = :userId) " +
            "AND (:fromDate IS NULL OR a.eventDate >= :fromDate) " +
            "AND (:toDate IS NULL OR a.eventDate <= :toDate)")
    Page<AuditLog> findFilteredLogs(@Param("eventType") String eventType,
                                    @Param("entityType") String entityType,
                                    @Param("entityId") UUID entityId,
                                    @Param("userId") UUID userId,
                                    @Param("fromDate") LocalDateTime fromDate,
                                    @Param("toDate") LocalDateTime toDate,
                                    Pageable pageable);
}
