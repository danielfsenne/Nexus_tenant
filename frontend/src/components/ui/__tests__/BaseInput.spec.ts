import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseInput from '../BaseInput.vue'

describe('BaseInput', () => {
  it('renderiza o label quando informado', () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '', label: 'Nome' } })

    expect(wrapper.find('label').text()).toBe('Nome')
  })

  it('emite update:modelValue com o valor digitado', async () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '' } })

    await wrapper.find('input').setValue('Cliente Teste')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['Cliente Teste'])
  })

  it('mostra a mensagem de erro quando a prop error é passada', () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '', error: 'Campo obrigatório' } })

    expect(wrapper.text()).toContain('Campo obrigatório')
    expect(wrapper.find('input').classes()).toContain('border-red-400')
  })

  it('reflete o atributo disabled', () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '', disabled: true } })

    expect(wrapper.find('input').attributes('disabled')).toBeDefined()
  })
})
