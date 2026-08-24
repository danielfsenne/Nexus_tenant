package com.nexus.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base para testes de integração: sobe um Postgres real via Testcontainers em vez
 * do H2 em modo de compatibilidade, e liga o Flyway de verdade — assim as
 * migrações (V1..V4) são exercitadas contra o banco de destino real, não uma
 * aproximação.
 *
 * O container é iniciado manualmente (sem @Testcontainers/@Container) e nunca
 * parado explicitamente: se usássemos o ciclo de vida automático do JUnit, o
 * @AfterAll de CADA classe de teste derrubaria o container estático
 * compartilhado, quebrando as classes de teste seguintes. O Ryuk do
 * Testcontainers limpa o container ao final da execução da JVM.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void integrationOverrides(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
