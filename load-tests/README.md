# Testes de carga (k6)

Testes de carga contra a API do Nexus, usando [k6](https://k6.io/). Não
exigem instalar nada além do Docker — rodam via `docker run`, contra a
stack já de pé em `docker compose up`.

## Antes de rodar

O rate limiter de autenticação (10 tentativas/60s em `/auth/login` e
`/auth/register`) atrapalha o `smoke.js` se ele for repetido várias vezes
seguidas, e o `api-load-test.js` só registra uma vez no `setup()`
(propositalmente, pra não esbarrar nesse limite). Para testes de carga
mais agressivos, considere subir a stack com
`NEXUS_RATE_LIMIT_ENABLED=false` no `.env`.

## Smoke test (rápido, checa se a API está no ar)

```sh
docker run --rm -i --network nexus_tenant_default \
  -e BASE_URL=http://nexus-backend:8080 \
  grafana/k6 run - < smoke.js
```

## Teste de carga (rampa até 30 VUs por ~2 minutos)

```sh
docker run --rm -i --network nexus_tenant_default \
  -e BASE_URL=http://nexus-backend:8080 \
  grafana/k6 run - < api-load-test.js
```

Cobre: listagem paginada de clientes, criação de cliente, criação de
venda (com `Idempotency-Key`), listagem paginada de produtos. Critérios
de aprovação (`thresholds`): p95 das requisições abaixo de 500ms e menos
de 1% de erro.

## Rodando fora do Docker

Se preferir instalar o k6 localmente ([instruções](https://k6.io/docs/get-started/installation/)):

```sh
k6 run -e BASE_URL=http://localhost:8080 api-load-test.js
```

## Acompanhando durante o teste

Com a stack no ar, dá pra observar o impacto em tempo real:
- Grafana (`http://localhost:3001`) — dashboard do backend e traces no Tempo.
- RabbitMQ Management (`http://localhost:15672`) — filas de processamento de vendas.
