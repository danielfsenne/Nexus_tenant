import { describe, it, expect } from 'vitest'
import { AxiosError } from 'axios'
import { extractErrorMessage } from '../http'

describe('extractErrorMessage', () => {
  it('retorna a mensagem vinda do backend quando presente', () => {
    const error = new AxiosError('Request failed', undefined, undefined, undefined, {
      status: 409,
      statusText: 'Conflict',
      headers: {},
      config: {} as never,
      data: { message: 'Já existe um cliente com este e-mail.' },
    })

    expect(extractErrorMessage(error)).toBe('Já existe um cliente com este e-mail.')
  })

  it('usa a mensagem de fallback quando o backend não manda "message"', () => {
    const error = new AxiosError('Request failed', undefined, undefined, undefined, {
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
      config: {} as never,
      data: {},
    })

    expect(extractErrorMessage(error, 'Não foi possível salvar.')).toBe('Não foi possível salvar.')
  })

  it('usa a mensagem de fallback para erros que não são do axios', () => {
    expect(extractErrorMessage(new Error('boom'), 'Algo deu errado.')).toBe('Algo deu errado.')
  })

  it('usa a mensagem padrão quando nenhum fallback é passado', () => {
    expect(extractErrorMessage(new Error('boom'))).toBe('Algo deu errado.')
  })
})
