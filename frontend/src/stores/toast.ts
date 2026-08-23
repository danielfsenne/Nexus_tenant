import { defineStore } from 'pinia'

export type ToastVariant = 'success' | 'error' | 'info'

export interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

let nextId = 1
const DEFAULT_DURATION_MS = 4000

export const useToastStore = defineStore('toast', {
  state: () => ({
    items: [] as Toast[],
  }),

  actions: {
    push(message: string, variant: ToastVariant = 'info', durationMs = DEFAULT_DURATION_MS) {
      const id = nextId++
      this.items.push({ id, message, variant })
      setTimeout(() => this.dismiss(id), durationMs)
    },

    success(message: string) {
      this.push(message, 'success')
    },

    error(message: string) {
      this.push(message, 'error')
    },

    info(message: string) {
      this.push(message, 'info')
    },

    dismiss(id: number) {
      this.items = this.items.filter((item) => item.id !== id)
    },
  },
})
