package com.nexus.backend.repository;

import com.nexus.backend.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByTenantIdAndIdempotencyKey(Long tenantId, String idempotencyKey);
}
