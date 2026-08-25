package com.nexus.backend.common;

import com.nexus.backend.domain.IdempotencyKey;
import com.nexus.backend.repository.IdempotencyKeyRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Cada método roda na própria transação (REQUIRES_NEW) porque o Postgres
 * "envenena" a transação inteira após uma violação de constraint única —
 * capturar a exceção na mesma transação deixaria comandos seguintes
 * (como reconsultar a chave do vencedor da corrida) falhando com
 * "current transaction is aborted".
 */
@Component
public class IdempotencyKeyStore {

    private final IdempotencyKeyRepository repository;

    public IdempotencyKeyStore(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    /**
     * Lança DataIntegrityViolationException se a chave já existir (constraint
     * única). Propositalmente não captura aqui: uma vez que o flush falha, o
     * Hibernate marca a transação como rollback-only, então "engolir" a
     * exceção dentro do próprio método transacional faria o commit seguinte
     * estourar UnexpectedRollbackException. Quem chama precisa capturar essa
     * exceção FORA da fronteira transacional (depois que o rollback já
     * aconteceu de forma limpa).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void claim(Long tenantId, String idempotencyKey) {
        IdempotencyKey key = IdempotencyKey.builder()
                .tenantId(tenantId)
                .idempotencyKey(idempotencyKey)
                .build();
        repository.saveAndFlush(key);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long tenantId, String idempotencyKey, int status, String responseBody) {
        repository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey).ifPresent(key -> {
            key.setResponseStatus(status);
            key.setResponseBody(responseBody);
            key.setCompletedAt(Instant.now());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void discard(Long tenantId, String idempotencyKey) {
        repository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey)
                .ifPresent(repository::delete);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<IdempotencyKey> find(Long tenantId, String idempotencyKey) {
        return repository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);
    }
}
