import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import router from '../router'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const toast = useToastStore()

    if (status === 401) {
      const auth = useAuthStore()
      auth.logout()
      router.push('/login')
    } else if (status === undefined) {
      toast.error('Não foi possível conectar ao servidor.')
    } else if (status === 403) {
      toast.error('Você não tem permissão para realizar essa ação.')
    } else if (status >= 500) {
      toast.error('Erro no servidor. Tente novamente em instantes.')
    }

    return Promise.reject(error)
  },
)

export function extractErrorMessage(error: unknown, fallback = 'Algo deu errado.'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined
    if (data?.message) return data.message
  }
  return fallback
}

export default http
