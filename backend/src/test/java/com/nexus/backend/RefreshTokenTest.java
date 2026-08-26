package com.nexus.backend;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RefreshTokenTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginRetornaAccessTokenERefreshToken() throws Exception {
        registrar("refresh-login@teste.com", "senha123");

        JsonNode body = login("refresh-login@teste.com", "senha123");

        assertThat(body.get("token").asText()).isNotBlank();
        assertThat(body.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void refreshTrocaOAccessTokenERevogaORefreshTokenAntigo() throws Exception {
        registrar("refresh-rotacao@teste.com", "senha123");
        JsonNode loginBody = login("refresh-rotacao@teste.com", "senha123");
        String refreshToken = loginBody.get("refreshToken").asText();

        String refreshResponse = mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode newBody = objectMapper.readTree(refreshResponse);
        assertThat(newBody.get("refreshToken").asText()).isNotEqualTo(refreshToken);

        // Reusar o refresh token já trocado deve falhar: cada um só é válido
        // para uma troca (rotação).
        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevogaORefreshTokenEBloqueiaNovoRefresh() throws Exception {
        registrar("refresh-logout@teste.com", "senha123");
        JsonNode loginBody = login("refresh-logout@teste.com", "senha123");
        String refreshToken = loginBody.get("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutAllRevogaTodasAsSessoesDoUsuario() throws Exception {
        registrar("refresh-logoutall@teste.com", "senha123");
        JsonNode loginA = login("refresh-logoutall@teste.com", "senha123");
        JsonNode loginB = login("refresh-logoutall@teste.com", "senha123");

        mockMvc.perform(post("/users/me/logout-all")
                        .header("Authorization", "Bearer " + loginB.get("token").asText()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", loginA.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", loginB.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshComTokenInexistenteEhRejeitado() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "token-que-nao-existe"))))
                .andExpect(status().isUnauthorized());
    }

    private void registrar(String email, String password) throws Exception {
        var body = Map.of(
                "companyName", "Empresa " + email,
                "adminName", "Admin",
                "email", email,
                "password", password
        );

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    private JsonNode login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response);
    }
}
