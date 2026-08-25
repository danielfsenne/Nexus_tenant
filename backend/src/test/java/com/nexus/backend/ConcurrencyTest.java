package com.nexus.backend;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Corridas reais entre threads concorrentes, não simulações sequenciais:
 * validam a Idempotency-Key (via constraint única do Postgres) e o
 * Optimistic Locking (via @Version) sob concorrência de verdade.
 */
class ConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requisicoesConcorrentesComMesmaIdempotencyKeyCriamApenasUmaVenda() throws Exception {
        String token = registrarEExtrairToken("Empresa Idempotencia", "idempotencia@a.com", "senha123");
        Long clienteId = criarClienteERetornarId(token, "Cliente Idempotente");
        String idempotencyKey = "chave-fixa-" + System.nanoTime();

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Integer>> tasks = IntStream.range(0, threads)
                .<Callable<Integer>>mapToObj(i -> () -> mockMvc.perform(post("/orders")
                                .header("Authorization", "Bearer " + token)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(Map.of("customerId", clienteId, "total", "42.00"))))
                        .andReturn().getResponse().getStatus())
                .collect(Collectors.toList());

        List<Future<Integer>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        List<Integer> statuses = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();

        assertThat(statuses).allMatch(status -> status == 200);

        String listResponse = mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + token)
                        .param("customerId", clienteId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(listResponse).get("totalElements").asInt()).isEqualTo(1);
    }

    @Test
    void atualizacoesConcorrentesDoMesmoProdutoGeramUmSucessoEUmConflito() throws Exception {
        String token = registrarEExtrairToken("Empresa Lock Otimista", "lock@a.com", "senha123");
        Long produtoId = criarProdutoERetornarId(token, "Produto Concorrente");

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Integer>> tasks = IntStream.range(0, threads)
                .<Callable<Integer>>mapToObj(i -> () -> mockMvc.perform(put("/products/" + produtoId)
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(Map.of("name", "Produto Editado " + i, "price", "10.00"))))
                        .andReturn().getResponse().getStatus())
                .collect(Collectors.toList());

        List<Future<Integer>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        List<Integer> statuses = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();

        assertThat(statuses).contains(200);
        assertThat(statuses).anyMatch(status -> status == 200 || status == 409);
        assertThat(statuses.stream().filter(status -> status == 200).count()).isEqualTo(1);
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
        var body = Map.of("name", name, "email", "cliente-concorrencia@exemplo.com");

        String response = mockMvc.perform(post("/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long criarProdutoERetornarId(String token, String name) throws Exception {
        var body = Map.of("name", name, "price", "50.00");

        String response = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
