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

  it('login salva o token, o refresh token e persiste no localStorage', async () => {
    mockedPost.mockResolvedValue({
      data: { token: 'jwt-fake', refreshToken: 'refresh-fake', tenantId: 7, role: 'ADMIN' },
    } as never)

    const auth = useAuthStore()
    await auth.login({ email: 'admin@teste.com', password: 'senha123' })

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.token).toBe('jwt-fake')
    expect(auth.refreshToken).toBe('refresh-fake')
    expect(auth.tenantId).toBe(7)
    expect(auth.role).toBe('ADMIN')
    expect(JSON.parse(localStorage.getItem('nexus.auth')!)).toMatchObject({ token: 'jwt-fake' })
  })

  it('register salva o token igual ao login', async () => {
    mockedPost.mockResolvedValue({
      data: { token: 'jwt-registro', refreshToken: 'refresh-registro', tenantId: 9, role: 'ADMIN' },
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

  it('refreshAccessToken troca o par de tokens usando o refresh token atual', async () => {
    mockedPost.mockResolvedValueOnce({
      data: { token: 'jwt-velho', refreshToken: 'refresh-velho', tenantId: 7, role: 'ADMIN' },
    } as never)

    const auth = useAuthStore()
    await auth.login({ email: 'admin@teste.com', password: 'senha123' })

    mockedPost.mockResolvedValueOnce({
      data: { token: 'jwt-novo', refreshToken: 'refresh-novo', tenantId: 7, role: 'ADMIN' },
    } as never)

    await auth.refreshAccessToken()

    expect(mockedPost).toHaveBeenLastCalledWith('/auth/refresh', { refreshToken: 'refresh-velho' })
    expect(auth.token).toBe('jwt-novo')
    expect(auth.refreshToken).toBe('refresh-novo')
  })

  it('refreshAccessToken lança quando não há refresh token salvo', async () => {
    const auth = useAuthStore()

    await expect(auth.refreshAccessToken()).rejects.toThrow()
    expect(mockedPost).not.toHaveBeenCalled()
  })

  it('logout limpa o estado, o localStorage e tenta revogar a sessão no backend', async () => {
    mockedPost.mockResolvedValue({
      data: { token: 'jwt-fake', refreshToken: 'refresh-fake', tenantId: 7, role: 'ADMIN' },
    } as never)

    const auth = useAuthStore()
    await auth.login({ email: 'admin@teste.com', password: 'senha123' })
    mockedPost.mockClear()
    auth.logout()

    expect(auth.isAuthenticated).toBe(false)
    expect(auth.token).toBeNull()
    expect(localStorage.getItem('nexus.auth')).toBeNull()
    expect(mockedPost).toHaveBeenCalledWith('/auth/logout', { refreshToken: 'refresh-fake' })
  })

  it('restaura a sessão salva no localStorage ao criar a store', () => {
    localStorage.setItem(
      'nexus.auth',
      JSON.stringify({ token: 'jwt-salvo', refreshToken: 'refresh-salvo', tenantId: 3, role: 'EMPLOYEE' }),
    )

    const auth = useAuthStore()

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.token).toBe('jwt-salvo')
    expect(auth.role).toBe('EMPLOYEE')
  })
})
