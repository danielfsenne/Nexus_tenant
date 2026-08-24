package com.nexus.backend;

import com.nexus.backend.domain.Invite;
import com.nexus.backend.repository.InviteRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cobre RBAC de ponta a ponta: convida um EMPLOYEE de verdade (em vez de só
 * checar a anotação @PreAuthorize), garante que ele consegue as ações que
 * o papel permite e é barrado nas que não permite.
 */
class AccessControlTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InviteRepository inviteRepository;

    @Test
    void employeeConsegueCriarMasNaoExcluirCliente() throws Exception {
        String tokenAdmin = registrarEExtrairToken("Empresa RBAC", "admin@rbac.com", "senha123");
        String tokenEmployee = convidarEAceitarComoEmployee(tokenAdmin, "funcionario@rbac.com");

        // EMPLOYEE pode criar cliente.
        Long clienteId = criarClienteERetornarId(tokenEmployee, "Cliente criado pelo employee");

        // EMPLOYEE não pode excluir (só ADMIN/MANAGER podem).
        mockMvc.perform(delete("/customers/" + clienteId)
                        .header("Authorization", "Bearer " + tokenEmployee))
                .andExpect(status().isForbidden());

        // ADMIN consegue excluir normalmente.
        mockMvc.perform(delete("/customers/" + clienteId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    void employeeNaoConsegueListarUsuarios() throws Exception {
        String tokenAdmin = registrarEExtrairToken("Empresa RBAC 2", "admin@rbac2.com", "senha123");
        String tokenEmployee = convidarEAceitarComoEmployee(tokenAdmin, "funcionario2@rbac2.com");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + tokenEmployee))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    private String convidarEAceitarComoEmployee(String tokenAdmin, String email) throws Exception {
        var inviteBody = Map.of("email", email, "role", "EMPLOYEE");
        mockMvc.perform(post("/invites")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inviteBody)))
                .andExpect(status().isOk());

        List<Invite> pendentes = inviteRepository.findAllByTenantIdAndAcceptedAtIsNull(tenantIdDoToken(tokenAdmin));
        String token = pendentes.stream()
                .filter(i -> i.getEmail().equals(email))
                .findFirst()
                .orElseThrow()
                .getToken();

        var acceptBody = Map.of("token", token, "name", "Funcionário", "password", "senha123");
        String response = mockMvc.perform(post("/invites/accept")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(acceptBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private Long tenantIdDoToken(String token) {
        String base64 = token.split("\\.")[1];
        String padded = base64 + "=".repeat((4 - base64.length() % 4) % 4);
        String payload = new String(java.util.Base64.getUrlDecoder().decode(padded));
        return objectMapper.readTree(payload).get("tenantId").asLong();
    }

    private String registrarEExtrairToken(String companyName, String email, String password) throws Exception {
        var body = Map.of(
                "companyName", companyName,
                "adminName", "Admin",
                "email", email,
                "password", password
        );

        String response = mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private Long criarClienteERetornarId(String token, String name) throws Exception {
        var body = Map.of("name", name, "email", "cliente@exemplo.com");

        String response = mockMvc.perform(post("/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
