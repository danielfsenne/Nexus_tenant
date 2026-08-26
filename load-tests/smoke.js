import http from 'k6/http'
import { check } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'

// Checagem rápida de 1 VU / algumas iterações — confirma que a API está no
// ar e respondendo antes de rodar o teste de carga de verdade.
export const options = {
  vus: 1,
  iterations: 5,
  thresholds: {
    http_req_duration: ['p(95)<300'],
    http_req_failed: ['rate==0'],
  },
}

export default function () {
  const res = http.get(`${BASE_URL}/actuator/health`)
  check(res, {
    'status 200': (r) => r.status === 200,
    'status UP': (r) => r.json('status') === 'UP',
  })
}
