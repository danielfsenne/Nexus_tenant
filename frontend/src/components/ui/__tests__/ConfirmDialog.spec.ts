import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfirmDialog from '../ConfirmDialog.vue'

// Stubar o Teleport faz o conteúdo renderizar no próprio wrapper em vez de
// ser movido para document.body, o que simplifica as consultas do teste.
const mountOptions = { global: { stubs: { teleport: true } } }

describe('ConfirmDialog', () => {
  it('não renderiza nada quando open é false', () => {
    const wrapper = mount(ConfirmDialog, {
      props: { open: false, title: 'Excluir cliente', message: 'Tem certeza?' },
      ...mountOptions,
    })

    expect(wrapper.text()).not.toContain('Excluir cliente')
  })

  it('mostra título e mensagem quando open é true', () => {
    const wrapper = mount(ConfirmDialog, {
      props: { open: true, title: 'Excluir cliente', message: 'Tem certeza?' },
      ...mountOptions,
    })

    expect(wrapper.text()).toContain('Excluir cliente')
    expect(wrapper.text()).toContain('Tem certeza?')
  })

  it('emite confirm ao clicar no botão de confirmar', async () => {
    const wrapper = mount(ConfirmDialog, {
      props: { open: true, title: 'Excluir', message: 'Tem certeza?' },
      ...mountOptions,
    })

    await wrapper.find('button:last-of-type').trigger('click')

    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })

  it('emite cancel ao clicar em cancelar, e não em confirmar', async () => {
    const wrapper = mount(ConfirmDialog, {
      props: { open: true, title: 'Excluir', message: 'Tem certeza?' },
      ...mountOptions,
    })

    await wrapper.find('button:first-of-type').trigger('click')

    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('confirm')).toBeUndefined()
  })

  it('desabilita o botão de confirmar quando loading é true', () => {
    const wrapper = mount(ConfirmDialog, {
      props: { open: true, title: 'Excluir', message: 'Tem certeza?', loading: true },
      ...mountOptions,
    })

    const confirmButton = wrapper.find('button:last-of-type')
    expect(confirmButton.attributes('disabled')).toBeDefined()
    expect(confirmButton.text()).toBe('Excluindo...')
  })
})
