package com.nexus.backend.security;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

/**
 * Aplica o tenant na sessão do Postgres (variável "app.tenant_id", usada
 * pelas policies de Row-Level Security) no meio de uma transação em
 * andamento, para os poucos fluxos públicos onde o tenant só é descoberto
 * depois que a conexão já foi obtida sem contexto (ex.: aceitar convite por
 * token, sem JWT). O TenantAwareDataSource já cobre automaticamente o caso
 * comum (requisição autenticada, tenant conhecido desde o início da
 * transação) — isso aqui é só para esses casos especiais.
 */
@Component
public class TenantSessionSync {

    private final EntityManager entityManager;

    public TenantSessionSync(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void apply(Long tenantId) {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement statement =
                    connection.prepareStatement("SELECT set_config('app.tenant_id', ?, false)")) {
                statement.setString(1, String.valueOf(tenantId));
                statement.execute();
            }
        });
    }
}
