package com.nexus.backend.repository;

import com.nexus.backend.domain.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);
}
