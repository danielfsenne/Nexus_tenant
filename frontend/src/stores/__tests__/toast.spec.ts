import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useToastStore } from '../toast'

describe('toast store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('adiciona um toast de sucesso', () => {
    const toast = useToastStore()
    toast.success('Cliente salvo.')

    expect(toast.items).toHaveLength(1)
    expect(toast.items[0]).toMatchObject({ message: 'Cliente salvo.', variant: 'success' })
  })

  it('adiciona toasts de erro e info com a variante correta', () => {
    const toast = useToastStore()
    toast.error('Falha ao salvar.')
    toast.info('Aviso qualquer.')

    expect(toast.items.map((item) => item.variant)).toEqual(['error', 'info'])
  })

  it('remove o toast automaticamente após a duração padrão', () => {
    const toast = useToastStore()
    toast.success('Some em 4s.')

    expect(toast.items).toHaveLength(1)

    vi.advanceTimersByTime(4000)

    expect(toast.items).toHaveLength(0)
  })

  it('dismiss remove só o toast pedido, mantendo os outros', () => {
    const toast = useToastStore()
    toast.success('Primeiro')
    toast.success('Segundo')
    const firstId = toast.items[0].id

    toast.dismiss(firstId)

    expect(toast.items).toHaveLength(1)
    expect(toast.items[0].message).toBe('Segundo')
  })
})
