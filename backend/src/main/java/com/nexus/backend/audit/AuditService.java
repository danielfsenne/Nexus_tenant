package com.nexus.backend.audit;

import com.nexus.backend.common.PageResponse;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.AuditLog;
import com.nexus.backend.repository.AuditLogRepository;
import com.nexus.backend.security.CurrentUserContext;
import com.nexus.backend.security.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public PageResponse<AuditLogResponse> findAll(int page, int size, AuditAction action) {
        var pageable = PageRequest.of(page, size);
        var tenantId = TenantContext.get();
        var result = (action == null)
                ? auditLogRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                : auditLogRepository.findAllByTenantIdAndActionOrderByCreatedAtDesc(tenantId, action, pageable);
        return PageResponse.from(result, AuditLogResponse::from);
    }

    /**
     * Registra um evento usando o tenant/usuário do contexto da requisição autenticada atual.
     */
    public void record(AuditAction action, String entityType, Long entityId, String details) {
        CurrentUserContext.CurrentUser currentUser = CurrentUserContext.get();
        recordForTenant(
                TenantContext.get(),
                currentUser != null ? currentUser.id() : null,
                currentUser != null ? currentUser.email() : null,
                action,
                entityType,
                entityId,
                details
        );
    }

    /**
     * Registra um evento explicitando tenant/usuário — usado em fluxos públicos (ex.: aceitar convite)
     * onde não há contexto de requisição autenticada.
     */
    public void recordForTenant(
            Long tenantId,
            Long userId,
            String userEmail,
            AuditAction action,
            String entityType,
            Long entityId,
            String details
    ) {
        auditLogRepository.save(
                AuditLog.builder()
                        .tenantId(tenantId)
                        .userId(userId)
                        .userEmail(userEmail)
                        .action(action)
                        .entityType(entityType)
                        .entityId(entityId)
                        .details(details)
                        .build()
        );
    }
}
