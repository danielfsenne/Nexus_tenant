import { defineStore } from 'pinia'
import http from '../lib/http'

export interface AuthResponse {
  token: string
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

    logout() {
      this.auth = null
      localStorage.removeItem(STORAGE_KEY)
    },
  },
})
