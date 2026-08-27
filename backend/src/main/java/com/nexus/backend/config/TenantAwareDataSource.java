package com.nexus.backend.config;

import com.nexus.backend.security.TenantContext;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Envolve o DataSource principal para aplicar o tenant da requisição atual
 * como variável de sessão do Postgres ("app.tenant_id") toda vez que uma
 * conexão é obtida do pool. As policies de Row-Level Security (migration V8)
 * usam essa variável para restringir linhas ao tenant certo mesmo que algum
 * código da aplicação esqueça o filtro por tenant_id.
 *
 * Como spring.jpa.open-in-view=false, a conexão é adquirida uma única vez no
 * início de cada transação e reaproveitada até o fim dela — então isso roda
 * uma vez por transação, não por query.
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    private static final String SET_TENANT_SQL = "SELECT set_config('app.tenant_id', ?, false)";
    private static final String RESET_TENANT_SQL = "RESET app.tenant_id";

    public TenantAwareDataSource(DataSource delegate) {
        super(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyTenant(connection);
        return connection;
    }

    private void applyTenant(Connection connection) throws SQLException {
        Long tenantId = TenantContext.getOrNull();
        if (tenantId == null) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(RESET_TENANT_SQL);
            }
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(SET_TENANT_SQL)) {
            statement.setString(1, String.valueOf(tenantId));
            statement.execute();
        }
    }
}
