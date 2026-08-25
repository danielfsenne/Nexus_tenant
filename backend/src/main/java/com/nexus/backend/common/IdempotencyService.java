package com.nexus.backend.common;

import com.nexus.backend.domain.IdempotencyKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Garante que requisições repetidas com a mesma Idempotency-Key não
 * dupliquem efeitos colaterais. Requisições verdadeiramente concorrentes
 * com a mesma chave são serializadas pelo índice único de
 * (tenant_id, idempotency_key) no Postgres: a segunda tentativa de INSERT
 * bloqueia até a primeira commitar, depois falha por violação de
 * constraint — quem perdeu a corrida então espera e devolve a resposta
 * de quem venceu, em vez de processar a requisição de novo.
 */
@Service
public class IdempotencyService {

    private static final int POLL_ATTEMPTS = 40;
    private static final long POLL_INTERVAL_MS = 50;

    private final IdempotencyKeyStore store;
    private final JsonMapper jsonMapper;

    public IdempotencyService(IdempotencyKeyStore store, JsonMapper jsonMapper) {
        this.store = store;
        this.jsonMapper = jsonMapper;
    }

    public <T> T execute(Long tenantId, String idempotencyKey, Class<T> responseType, Supplier<T> action) {
        if (store.find(tenantId, idempotencyKey).isPresent()) {
            return awaitResponse(tenantId, idempotencyKey, responseType);
        }

        try {
            store.claim(tenantId, idempotencyKey);
        } catch (DataIntegrityViolationException ex) {
            return awaitResponse(tenantId, idempotencyKey, responseType);
        }

        try {
            T result = action.get();
            store.complete(tenantId, idempotencyKey, 200, jsonMapper.writeValueAsString(result));
            return result;
        } catch (RuntimeException ex) {
            store.discard(tenantId, idempotencyKey);
            throw ex;
        }
    }

    private <T> T awaitResponse(Long tenantId, String idempotencyKey, Class<T> responseType) {
        for (int attempt = 0; attempt < POLL_ATTEMPTS; attempt++) {
            Optional<IdempotencyKey> key = store.find(tenantId, idempotencyKey);
            if (key.isPresent() && key.get().getCompletedAt() != null) {
                return jsonMapper.readValue(key.get().getResponseBody(), responseType);
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ConflictException("Requisição interrompida.");
            }
        }
        throw new ConflictException("Tempo esgotado aguardando o processamento da requisição idempotente.");
    }
}
