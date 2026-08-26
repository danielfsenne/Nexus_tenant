import { defineStore } from 'pinia'
import http from '../lib/http'

export interface AuthResponse {
  token: string
  refreshToken: string
  tenantId: number
  role: 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
}

export interface RegisterPayload {
  companyName: string
  adminName: string
  email: string
  password: string
}

export interface LoginPayload {
  email: string
  password: string
}

const STORAGE_KEY = 'nexus.auth'

function loadFromStorage(): AuthResponse | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  return raw ? (JSON.parse(raw) as AuthResponse) : null
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    auth: loadFromStorage() as AuthResponse | null,
  }),

  getters: {
    token: (state) => state.auth?.token ?? null,
    refreshToken: (state) => state.auth?.refreshToken ?? null,
    tenantId: (state) => state.auth?.tenantId ?? null,
    role: (state) => state.auth?.role ?? null,
    isAuthenticated: (state) => state.auth !== null,
  },

  actions: {
    setAuth(auth: AuthResponse) {
      this.auth = auth
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
    },

    async login(payload: LoginPayload) {
      const { data } = await http.post<AuthResponse>('/auth/login', payload)
      this.setAuth(data)
    },

    async register(payload: RegisterPayload) {
      const { data } = await http.post<AuthResponse>('/auth/register', payload)
      this.setAuth(data)
    },

    // Troca o refresh token atual por um par novo (rotação). Lança se o
    // refresh token não existir ou já tiver sido usado/expirado — quem
    // chama decide o que fazer (normalmente, deslogar).
    async refreshAccessToken() {
      const currentRefreshToken = this.auth?.refreshToken
      if (!currentRefreshToken) throw new Error('Sem refresh token disponível.')

      const { data } = await http.post<AuthResponse>('/auth/refresh', { refreshToken: currentRefreshToken })
      this.setAuth(data)
    },

    logout() {
      const refreshToken = this.auth?.refreshToken
      this.auth = null
      localStorage.removeItem(STORAGE_KEY)

      if (refreshToken) {
        // Melhor esforço: revoga a sessão no backend, mas não bloqueia o
        // logout local por causa disso.
        http.post('/auth/logout', { refreshToken }).catch(() => {})
      }
    },
  },
})
