# Nexus

SaaS de gestão empresarial multi-tenant (multi-empresa) com isolamento de dados por `tenant_id`, RBAC por papéis (Admin/Manager/Employee) e planos de assinatura.

## Stack

- **Backend**: Java 17, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Flyway
- **Frontend**: Vue 3, TypeScript (em breve)
- **Infra**: Docker Compose (dev)

## Rodando localmente

```bash
docker compose up -d          # sobe o Postgres
cd backend
./mvnw spring-boot:run        # sobe a API
```

## Arquitetura

Multi-tenancy via banco compartilhado + coluna `tenant_id` em todas as tabelas de negócio. Todo acesso a dados é escopado pelo tenant do usuário autenticado (extraído do JWT), nunca confiando em IDs vindos do cliente sem validar o `tenant_id`.

## Status

Projeto em desenvolvimento incremental. Veja commits para acompanhar a evolução (MVP → multi-tenancy/RBAC → cache/filas → observabilidade).
