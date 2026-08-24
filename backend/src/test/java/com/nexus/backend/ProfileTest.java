package com.nexus.backend;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void trocaDeSenhaExigeSenhaAtualCorreta() throws Exception {
        String email = "admin@perfil.com";
        String token = registrarEExtrairToken("Empresa Perfil", email, "senhaOriginal1");

        // Senha atual errada é rejeitada e a senha não muda.
        var senhaErrada = Map.of("currentPassword", "senhaErrada", "newPassword", "senhaNova123");
        mockMvc.perform(put("/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(senhaErrada)))
                .andExpect(status().isUnauthorized());

        // Senha atual correta troca a senha.
        var senhaCorreta = Map.of("currentPassword", "senhaOriginal1", "newPassword", "senhaNova123");
        mockMvc.perform(put("/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(senhaCorreta)))
                .andExpect(status().isNoContent());

        // A senha antiga não autentica mais.
        var loginAntigo = Map.of("email", email, "password", "senhaOriginal1");
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginAntigo)))
                .andExpect(status().isUnauthorized());

        // A senha nova autentica normalmente.
        var loginNovo = Map.of("email", email, "password", "senhaNova123");
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginNovo)))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarNomeReflenteNoProprioPerfil() throws Exception {
        String token = registrarEExtrairToken("Empresa Perfil 2", "admin@perfil2.com", "senha123");

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "Novo Nome"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"));
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
}
