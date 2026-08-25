package com.nexus.backend.repository;

import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<AuditLog> findAllByTenantIdAndActionOrderByCreatedAtDesc(Long tenantId, AuditAction action, Pageable pageable);
}
