package com.nexus.backend;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usuarioDeUmaEmpresaNaoConsegueVerClientesDeOutraEmpresa() throws Exception {
        String tokenEmpresaA = registrarEExtrairToken("Empresa A", "admin@a.com", "senha123");
        String tokenEmpresaB = registrarEExtrairToken("Empresa B", "admin@b.com", "senha123");

        Long clienteIdEmpresaA = criarClienteERetornarId(tokenEmpresaA, "Cliente da Empresa A");

        // Empresa A enxerga o próprio cliente normalmente.
        mockMvc.perform(get("/customers/" + clienteIdEmpresaA)
                        .header("Authorization", "Bearer " + tokenEmpresaA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente da Empresa A"));

        // Empresa B tenta acessar o mesmo ID manipulando a URL e deve receber 404,
        // nunca o dado de outro tenant.
        mockMvc.perform(get("/customers/" + clienteIdEmpresaA)
                        .header("Authorization", "Bearer " + tokenEmpresaB))
                .andExpect(status().isNotFound());

        // A listagem da Empresa B não deve conter nenhum cliente da Empresa A.
        mockMvc.perform(get("/customers")
                        .header("Authorization", "Bearer " + tokenEmpresaB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void requisicaoSemTokenEhRejeitada() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isForbidden());
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
