# Nexus

SaaS de gestão empresarial multi-tenant (multi-empresa): autenticação, controle de acesso por papel, clientes, produtos, vendas, convites de equipe, auditoria, notificações em tempo real e processamento assíncrono de pedidos — com isolamento de dados por `tenant_id` em todas as camadas.

## Stack

**Backend** — Java 17, Spring Boot 4, Spring Security (JWT + refresh token), Spring Data JPA, PostgreSQL 16, Flyway, Redis, RabbitMQ, WebSocket/STOMP, Spring Mail, Actuator + Micrometer (Prometheus) + OpenTelemetry (tracing distribuído), springdoc-openapi (Swagger), JUnit 5 + Testcontainers.

**Frontend** — Vue 3 + TypeScript, Vite, Tailwind CSS, Pinia, Vue Router, Axios, STOMP.js, Playwright (E2E), Vitest (unitários).

**Infra** — Docker Compose (Postgres, Redis, RabbitMQ, backend, frontend, Prometheus, Grafana, Tempo), GitHub Actions (CI/CD), Blueprint de deploy no Render, testes de carga (k6), scripts de backup/restore.

## Funcionalidades

- **Autenticação e multi-tenancy** — registro de empresa com verificação de e-mail, login com JWT de vida curta + refresh token rotativo (com "sair de todos os dispositivos"), RBAC por papel (`ADMIN` / `MANAGER` / `EMPLOYEE`), isolamento de dados por tenant validado em toda requisição e reforçado com Row-Level Security no Postgres.
- **Gestão** — CRUD de clientes, produtos e vendas, com paginação, busca/filtro e exportação CSV.
- **SaaS** — planos com limites de uso (checados com lock pessimista contra corridas), convite de membros da equipe por e-mail, redefinição de senha por e-mail.
- **Confiabilidade de dados** — idempotência na criação de vendas (`Idempotency-Key`), padrão Outbox para publicação de eventos, optimistic locking (`@Version`) e soft-delete em clientes/produtos.
- **Observabilidade de produto** — log de auditoria por tenant, cache e rate-limiting via Redis, notificações em tempo real via WebSocket.
- **Processamento assíncrono** — vendas processadas via RabbitMQ com retry exponencial e dead-letter queue, jobs agendados de manutenção.
- **Perfil** — edição de dados próprios, troca de senha e encerramento de sessões.
- **Produção** — Docker completo, CI/CD, testes de integração contra Postgres real (Testcontainers), testes de concorrência, unitários (Vitest) e E2E (Playwright), testes de carga (k6), métricas Prometheus/Grafana, tracing distribuído (OpenTelemetry/Tempo), documentação OpenAPI/Swagger, deploy em nuvem (Render) e backup/restore do banco.

## Arquitetura

Multi-tenancy via banco compartilhado + coluna `tenant_id` em todas as tabelas de negócio. O `tenant_id` e o papel do usuário vêm das claims do JWT (nunca de um valor enviado pelo cliente) e ficam disponíveis via `TenantContext`/`CurrentUserContext` (ThreadLocal) durante toda a requisição; todo acesso a dado é escopado por esse tenant.

Isolamento em duas camadas: o filtro por `tenant_id` no código é a primeira, e Row-Level Security no Postgres é a segunda — as tabelas `customers`, `products`, `orders` e `audit_logs` têm policies que restringem as linhas visíveis à variável de sessão `app.tenant_id`, aplicada automaticamente a cada conexão pelo pool (`TenantAwareDataSource`). Isso significa que uma query nova que esqueça o filtro por tenant não vaza dados entre empresas — o banco barra mesmo assim. O pool da aplicação roda com uma role restrita (`nexus_app`, sem `SUPERUSER`/`BYPASSRLS`), separada da role usada só pelo Flyway para migrations.

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
| Tempo (traces, via Grafana) | http://localhost:3200 |
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

# unitários do frontend (Vitest)
cd frontend
npm run test:unit

# E2E do frontend — requer o backend rodando em localhost:8080
cd frontend
npm run test:e2e
```

Testes de carga (k6) contra a stack via Docker: veja `load-tests/README.md`.

## CI/CD

GitHub Actions roda em todo push/PR para `main`: testes do backend, testes unitários + build + type-check do frontend, e testes E2E contra a stack completa via `docker compose`. Em push para `main`, com tudo passando, as imagens Docker de backend e frontend são publicadas no GitHub Container Registry.

## Deploy em nuvem

Blueprint pronto para o [Render](https://render.com) em `render.yaml`. Passo a passo completo e pontos de atenção em [`DEPLOY.md`](./DEPLOY.md).

## Backup e restore

```bash
./scripts/backup.sh                              # backup do Postgres local (docker compose)
./scripts/restore.sh backups/nexus_TIMESTAMP.sql.gz

# contra um banco remoto (ex.: Render):
DATABASE_URL="postgres://..." ./scripts/backup.sh
```

## Estrutura do repositório

```
backend/          API Spring Boot
frontend/         SPA Vue 3
  e2e/            testes E2E (Playwright)
  src/**/__tests__  testes unitários (Vitest)
observability/    provisionamento do Prometheus, Grafana e Tempo
load-tests/       testes de carga (k6)
scripts/          backup.sh / restore.sh do Postgres
.github/workflows/  pipeline de CI/CD
docker-compose.yml  stack completa (dev/local)
render.yaml       Blueprint de deploy no Render
DEPLOY.md         passo a passo do deploy em nuvem
```
