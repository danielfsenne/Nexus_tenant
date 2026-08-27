# Deploy no Render

Este projeto tem um Blueprint (`render.yaml`) pronto pra subir a stack
inteira no [Render](https://render.com). Como eu não tenho acesso à sua
conta, este documento é o passo a passo pra você mesmo rodar — e a lista
de pontos que valem uma conferência manual no primeiro deploy, já que
nunca foi testado contra uma conta real.

## Passo a passo

1. Crie uma conta no Render (se ainda não tiver) e conecte seu GitHub.
2. No dashboard, **New > Blueprint**, aponte para o repositório
   `Nexus_tenant` na branch `main`. O Render lê o `render.yaml` da raiz
   automaticamente.
3. Revise os serviços que ele vai criar (aparecem numa tela de preview
   antes de confirmar):
   - `nexus-db` — Postgres gerenciado (plano free).
   - `nexus-redis` — Key Value (Redis-compatível) gerenciado.
   - `nexus-rabbitmq` — serviço privado rodando a imagem oficial do RabbitMQ.
   - `nexus-backend` — Spring Boot, buildado do `backend/Dockerfile`.
   - `nexus-frontend` — Vue/Nginx, buildado do `frontend/Dockerfile`.
4. Confirme e acompanhe o build. O backend demora mais (build Maven).

## Pontos pra conferir no primeiro deploy

- **`VITE_API_URL` do frontend**: o Render deve propagar essa `envVar`
  como build arg do Docker automaticamente (o `frontend/Dockerfile` já
  declara `ARG VITE_API_URL`). Se o bundle final não estiver apontando pro
  backend certo (dá pra checar no DevTools do navegador, aba Network, se
  as chamadas API vão para `https://nexus-backend.onrender.com`), confira
  em **nexus-frontend > Environment** se a variável está lá, e se não
  ajudar, veja se o Render tem uma seção separada de "Docker Build
  Arguments" na versão atual do dashboard.
- **Nomes dos serviços**: `NEXUS_FRONTEND_URL`, `NEXUS_CORS_ALLOWED_ORIGINS`
  e `VITE_API_URL` no `render.yaml` têm as URLs de `nexus-backend` e
  `nexus-frontend` escritas à mão (`https://<nome>.onrender.com`), porque
  o Render só permite referenciar propriedades internas (`host`, `port`)
  via `fromService`, não a URL pública. Se você renomear qualquer um dos
  dois serviços no Blueprint, atualize essas três variáveis manualmente.
- **RabbitMQ**: sobe como serviço privado, sem management UI acessível de
  fora. Pra inspecionar filas em produção, use `render ssh` no serviço ou
  temporariamente adicione uma porta pública só pra debugar.
- **Cold starts**: no plano free, backend e frontend "dormem" após um
  tempo sem tráfego e demoram alguns segundos pra acordar na próxima
  requisição — normal, não é bug.
- **Postgres free**: confira na página de preços do Render as condições
  atuais do plano gratuito (validade, limite de armazenamento) antes de
  depender dele pra dados que importam de verdade.

## E-mail em produção

`NEXUS_MAIL_ENABLED` está `false` no Blueprint (só loga, não envia). Pra
ligar de verdade, defina manualmente no dashboard do `nexus-backend`:
`NEXUS_MAIL_ENABLED=true`, `NEXUS_MAIL_USERNAME` e `NEXUS_MAIL_PASSWORD`
(senha de app do Gmail) — nunca coloque isso no `render.yaml` versionado.

## Tracing distribuído em produção

Por padrão `NEXUS_TRACING_SAMPLING_PROBABILITY=0` no Render, porque não
tem nenhum coletor OTLP rodando lá (o Tempo só existe no
`docker-compose.yml` local). Se quiser ver traces do ambiente de
produção, aponte `NEXUS_OTLP_TRACING_ENDPOINT` para um coletor real —
por exemplo, o tier gratuito do
[Grafana Cloud](https://grafana.com/products/cloud/) tem um endpoint OTLP
— e suba `NEXUS_TRACING_SAMPLING_PROBABILITY` para algo como `0.1`.

## Row-Level Security em produção

A migration V8 cria uma role de banco restrita (`nexus_app`, sem
`SUPERUSER`/`BYPASSRLS`) usada pelo pool de conexões da aplicação, e ativa
Row-Level Security nas tabelas `customers`, `products`, `orders` e
`audit_logs`. Isso exige que a role principal (a que roda as migrations,
`NEXUS_DB_USER`) tenha privilégio de `CREATE ROLE` — o que costuma faltar
em bancos gerenciados por provedores de nuvem, incluindo potencialmente o
Postgres free do Render.

A migration só tenta criar a role se ela ainda não existir, então dá pra
evitar o erro de vez criando-a manualmente **antes** do primeiro deploy
(ou antes de reativar o deploy, se já tiver falhado uma vez):

1. Pegue o valor de `NEXUS_DB_APP_PASSWORD` em **nexus-backend > Environment**
   no dashboard do Render (foi gerado automaticamente pelo Blueprint).
2. Conecte no `nexus-db` com um cliente que tenha privilégio administrativo
   (via **Connect** no dashboard do Render, ou `render psql`).
3. Rode: `CREATE ROLE nexus_app LOGIN PASSWORD '<valor do passo 1>' NOSUPERUSER NOBYPASSRLS;`
4. Faça o deploy (ou "Manual Deploy > Clear build cache & deploy" se já
   tinha falhado antes) — a migration vai ver que a role já existe, pular
   a criação e seguir com os `GRANT`/`CREATE POLICY` normalmente.

Se o deploy já tiver falhado e o Flyway tiver marcado a migration V8 como
`failed` no histórico (`flyway_schema_history`), rodar `./mvnw flyway:repair`
localmente contra o banco do Render (com `-Dflyway.url/user/password`
apontando pra lá) antes de tentar de novo.

## Backup do banco em produção

`scripts/backup.sh` e `scripts/restore.sh` aceitam `DATABASE_URL` pra
trabalhar contra o Postgres do Render em vez do container local:

```sh
DATABASE_URL="<connection string do nexus-db>" ./scripts/backup.sh
```

A connection string está em **nexus-db > Connect** no dashboard do Render.
