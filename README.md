# Nexus

SaaS de gestão empresarial multi-tenant (multi-empresa): autenticação, controle de acesso por papel, clientes, produtos, vendas, convites de equipe, auditoria, notificações em tempo real e processamento assíncrono de pedidos — com isolamento de dados por `tenant_id` em todas as camadas.

## Stack

**Backend** — Java 17, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL 16, Flyway, Redis, RabbitMQ, WebSocket/STOMP, Spring Mail, Actuator + Micrometer (Prometheus), springdoc-openapi (Swagger), JUnit 5 + Testcontainers.

**Frontend** — Vue 3 + TypeScript, Vite, Tailwind CSS, Pinia, Vue Router, Axios, STOMP.js, Playwright (E2E).

**Infra** — Docker Compose (Postgres, Redis, RabbitMQ, backend, frontend, Prometheus, Grafana), GitHub Actions (CI/CD).

## Funcionalidades

- **Autenticação e multi-tenancy** — registro de empresa, login com JWT, RBAC por papel (`ADMIN` / `MANAGER` / `EMPLOYEE`), isolamento de dados por tenant validado em toda requisição.
- **Gestão** — CRUD de clientes, produtos e vendas, com paginação server-side.
- **SaaS** — planos com limites de uso, convite de membros da equipe por e-mail, redefinição de senha por e-mail.
- **Observabilidade de produto** — log de auditoria por tenant, cache e rate-limiting via Redis, notificações em tempo real via WebSocket.
- **Processamento assíncrono** — vendas processadas via RabbitMQ com retry exponencial e dead-letter queue, jobs agendados de manutenção.
- **Perfil** — edição de dados próprios e troca de senha.
- **Produção** — Docker completo, CI/CD, testes de integração contra Postgres real (Testcontainers) e E2E (Playwright), métricas Prometheus/Grafana, documentação OpenAPI/Swagger.

## Arquitetura

Multi-tenancy via banco compartilhado + coluna `tenant_id` em todas as tabelas de negócio. O `tenant_id` e o papel do usuário vêm das claims do JWT (nunca de um valor enviado pelo cliente) e ficam disponíveis via `TenantContext`/`CurrentUserContext` (ThreadLocal) durante toda a requisição; todo acesso a dado é escopado por esse tenant.

Vendas são publicadas no RabbitMQ (`exchange nexus.orders`) e processadas de forma assíncrona por um consumer, com retry com backoff exponencial (TTL por mensagem + dead-letter-exchange) e uma fila de DLQ final após esgotar as tentativas. Notificações de eventos (nova venda, convite aceito, etc.) chegam ao frontend em tempo real via WebSocket/STOMP, num tópico escopado por tenant.

## Como rodar

### Stack completa via Docker

```bash
cp .env.example .env
# edite o .env: pelo menos NEXUS_JWT_SECRET precisa de um valor
docker compose up -d --build
```

| Serviço | URL |
|---|---|
| Frontend | http://localhost:8082 |
| API | http://localhost:8080 |
| Documentação da API (Swagger) | http://localhost:8080/swagger-ui/index.html |
| Grafana | http://localhost:3001 (usuário `admin`, senha em `NEXUS_GRAFANA_PASSWORD`) |
| Prometheus | http://localhost:9090 |
| RabbitMQ management | http://localhost:15672 (usuário/senha `nexus`) |

Veja `.env.example` para todas as variáveis disponíveis (e-mail transacional, CORS, rate-limit, etc.).

### Desenvolvimento local (hot reload)

```bash
# infraestrutura apenas
docker compose up -d postgres redis rabbitmq

# backend (porta 8080)
cd backend
./mvnw spring-boot:run

# frontend (porta 5173, hot reload)
cd frontend
npm install
npm run dev
```

## Testes

```bash
# backend — sobe um Postgres real via Testcontainers automaticamente (requer Docker)
cd backend
./mvnw test

# E2E do frontend — requer o backend rodando em localhost:8080
cd frontend
npm run test:e2e
```

## CI/CD

GitHub Actions roda em todo push/PR para `main`: testes do backend, build + type-check do frontend, e testes E2E contra a stack completa via `docker compose`. Em push para `main`, com tudo passando, as imagens Docker de backend e frontend são publicadas no GitHub Container Registry.

## Estrutura do repositório

```
backend/          API Spring Boot
frontend/         SPA Vue 3
  e2e/            testes E2E (Playwright)
observability/    provisionamento do Prometheus e Grafana
.github/workflows/  pipeline de CI/CD
docker-compose.yml  stack completa (dev/local)
```
