import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('../../lib/http', () => ({
  default: { post: vi.fn() },
}))

import http from '../../lib/http'
import { useAuthStore } from '../auth'

const mockedPost = vi.mocked(http.post)

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
    mockedPost.mockReset()
  })

  it('começa deslogado quando não há nada salvo', () => {
    const auth = useAuthStore()

    expect(auth.isAuthenticated).toBe(false)
    expect(auth.token).toBeNull()
  })

  it('login salva o token e persiste no localStorage', async () => {
    mockedPost.mockResolvedValue({
      data: { token: 'jwt-fake', tenantId: 7, role: 'ADMIN' },
    } as never)

    const auth = useAuthStore()
    await auth.login({ email: 'admin@teste.com', password: 'senha123' })

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.token).toBe('jwt-fake')
    expect(auth.tenantId).toBe(7)
    expect(auth.role).toBe('ADMIN')
    expect(JSON.parse(localStorage.getItem('nexus.auth')!)).toMatchObject({ token: 'jwt-fake' })
  })

  it('register salva o token igual ao login', async () => {
    mockedPost.mockResolvedValue({
      data: { token: 'jwt-registro', tenantId: 9, role: 'ADMIN' },
    } as never)

    const auth = useAuthStore()
    await auth.register({ companyName: 'Empresa', adminName: 'Admin', email: 'a@a.com', password: 'senha123' })

    expect(auth.token).toBe('jwt-registro')
    expect(mockedPost).toHaveBeenCalledWith('/auth/register', {
      companyName: 'Empresa',
      adminName: 'Admin',
      email: 'a@a.com',
      password: 'senha123',
    })
  })

  it('logout limpa o estado e o localStorage', async () => {
    mockedPost.mockResolvedValue({
      data: { token: 'jwt-fake', tenantId: 7, role: 'ADMIN' },
    } as never)

    const auth = useAuthStore()
    await auth.login({ email: 'admin@teste.com', password: 'senha123' })
    auth.logout()

    expect(auth.isAuthenticated).toBe(false)
    expect(auth.token).toBeNull()
    expect(localStorage.getItem('nexus.auth')).toBeNull()
  })

  it('restaura a sessão salva no localStorage ao criar a store', () => {
    localStorage.setItem('nexus.auth', JSON.stringify({ token: 'jwt-salvo', tenantId: 3, role: 'EMPLOYEE' }))

    const auth = useAuthStore()

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.token).toBe('jwt-salvo')
    expect(auth.role).toBe('EMPLOYEE')
  })
})
