package com.nexus.backend;

import com.nexus.backend.security.TenantContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirma que o isolamento entre tenants não depende só do filtro por
 * tenant_id no código da aplicação: mesmo consultando a tabela direto (sem
 * WHERE tenant_id), a policy de Row-Level Security do Postgres (migration V8)
 * restringe as linhas visíveis à sessão atual.
 */
class RowLevelSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void limparContextoDeTenant() {
        TenantContext.clear();
    }

    @Test
    void consultaDiretaSemFiltroPorTenantSoVeLinhasDoTenantDaSessao() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String nomeClienteA = "Cliente RLS A " + UUID.randomUUID();
        String nomeClienteB = "Cliente RLS B " + UUID.randomUUID();

        Long tenantA = registrarECriarCliente("rls-a-" + UUID.randomUUID() + "@teste.com", nomeClienteA);
        Long tenantB = registrarECriarCliente("rls-b-" + UUID.randomUUID() + "@teste.com", nomeClienteB);

        // Sessão sem tenant no contexto: nenhuma linha visível (fail-closed).
        TenantContext.clear();
        Integer semTenant = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM customers WHERE name IN (?, ?)", Integer.class, nomeClienteA, nomeClienteB);
        assertThat(semTenant).isZero();

        // Sessão no tenant A: só o cliente do tenant A aparece, mesmo sem
        // filtrar por tenant_id na query.
        TenantContext.set(tenantA);
        Integer vistoPorA = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM customers WHERE name IN (?, ?)", Integer.class, nomeClienteA, nomeClienteB);
        assertThat(vistoPorA).isEqualTo(1);

        // Sessão no tenant B: só o cliente do tenant B.
        TenantContext.set(tenantB);
        Integer vistoPorB = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM customers WHERE name IN (?, ?)", Integer.class, nomeClienteA, nomeClienteB);
        assertThat(vistoPorB).isEqualTo(1);
    }

    @Test
    void registroDeNovaEmpresaContinuaFuncionandoComRlsAtivo() throws Exception {
        var body = Map.of(
                "companyName", "Empresa RLS",
                "adminName", "Admin",
                "email", "rls-registro-" + UUID.randomUUID() + "@teste.com",
                "password", "senha123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    private Long registrarECriarCliente(String email, String nomeCliente) throws Exception {
        var registerBody = Map.of(
                "companyName", "Empresa " + email,
                "adminName", "Admin",
                "email", email,
                "password", "senha123"
        );

        String registerResponse = mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        String token = registerJson.get("token").asText();
        Long tenantId = registerJson.get("tenantId").asLong();

        mockMvc.perform(post("/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", nomeCliente, "email", ""))))
                .andExpect(status().isOk());

        return tenantId;
    }
}
