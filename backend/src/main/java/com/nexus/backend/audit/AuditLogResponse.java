package com.nexus.backend.audit;

import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.AuditLog;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String userEmail,
        AuditAction action,
        String entityType,
        Long entityId,
        String details,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserEmail(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
