package com.nexus.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base para testes de integração: sobe um Postgres real via Testcontainers em vez
 * do H2 em modo de compatibilidade, e liga o Flyway de verdade — assim as
 * migrações (V1..V8) são exercitadas contra o banco de destino real, não uma
 * aproximação.
 *
 * O container é iniciado manualmente (sem @Testcontainers/@Container) e nunca
 * parado explicitamente: se usássemos o ciclo de vida automático do JUnit, o
 * @AfterAll de CADA classe de teste derrubaria o container estático
 * compartilhado, quebrando as classes de teste seguintes. O Ryuk do
 * Testcontainers limpa o container ao final da execução da JVM.
 *
 * Sem @ServiceConnection aqui de propósito: desde a migration V8 (Row-Level
 * Security), a aplicação usa duas roles diferentes contra o mesmo banco — o
 * Flyway roda como o superuser do container (precisa de DDL/CREATE ROLE),
 * enquanto o pool principal (spring.datasource.*) roda como a role restrita
 * nexus_app criada por essa migration. Isso é configurado manualmente abaixo
 * em vez de deixar o @ServiceConnection apontar as duas coisas pro mesmo
 * usuário, senão os testes de integração nunca exercitariam o RLS de verdade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final String APP_ROLE_PASSWORD = "nexus_app";

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void integrationOverrides(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("spring.flyway.placeholders.appDbPassword", () -> APP_ROLE_PASSWORD);

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "nexus_app");
        registry.add("spring.datasource.password", () -> APP_ROLE_PASSWORD);
        // O profile "test" (application-test.yml) fixa driver-class-name pro
        // H2; sem @ServiceConnection aqui (ver comentário acima), precisa
        // sobrescrever manualmente pro Postgres do Testcontainers.
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
