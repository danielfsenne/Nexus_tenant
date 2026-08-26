import http from 'k6/http'
import { check, sleep } from 'k6'
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 30 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
}

// Registra UM tenant/admin uma única vez, no setup(), e reaproveita o token
// em todas as VUs — bater em /auth/register repetidamente esbarraria no
// rate limiter (10 tentativas/60s) e não mede o que importa aqui: o
// desempenho das rotas de negócio já autenticadas.
export function setup() {
  const suffix = randomString(8)
  const registerRes = http.post(
    `${BASE_URL}/auth/register`,
    JSON.stringify({
      companyName: `Empresa Carga ${suffix}`,
      adminName: 'Admin Carga',
      email: `carga-${suffix}@teste.com`,
      password: 'senha123',
    }),
    { headers: { 'Content-Type': 'application/json' } },
  )

  check(registerRes, { 'registro OK': (r) => r.status === 200 })

  const token = registerRes.json('token')

  const customerRes = http.post(
    `${BASE_URL}/customers`,
    JSON.stringify({ name: 'Cliente Carga', email: `cliente-carga-${suffix}@teste.com` }),
    { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } },
  )

  return { token, customerId: customerRes.json('id') }
}

export default function (data) {
  const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${data.token}` }

  const listCustomers = http.get(`${BASE_URL}/customers?page=0&size=10`, { headers })
  check(listCustomers, { 'lista de clientes OK': (r) => r.status === 200 })

  const createCustomer = http.post(
    `${BASE_URL}/customers`,
    JSON.stringify({ name: `Cliente ${randomString(6)}`, email: `${randomString(10)}@teste.com` }),
    { headers },
  )
  check(createCustomer, { 'criação de cliente OK': (r) => r.status === 200 })

  const createOrder = http.post(
    `${BASE_URL}/orders`,
    JSON.stringify({ customerId: data.customerId, total: (Math.random() * 500).toFixed(2) }),
    { headers: { ...headers, 'Idempotency-Key': `${__VU}-${__ITER}-${randomString(8)}` } },
  )
  check(createOrder, { 'criação de venda OK': (r) => r.status === 200 })

  const listProducts = http.get(`${BASE_URL}/products?page=0&size=10`, { headers })
  check(listProducts, { 'lista de produtos OK': (r) => r.status === 200 })

  sleep(1)
}
