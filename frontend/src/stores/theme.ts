import { defineStore } from 'pinia'

const STORAGE_KEY = 'nexus.theme'

function prefersDark() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

function loadInitial(): boolean {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored) return stored === 'dark'
  return prefersDark()
}

function applyToDocument(dark: boolean) {
  document.documentElement.classList.toggle('dark', dark)
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    dark: loadInitial(),
  }),

  actions: {
    init() {
      applyToDocument(this.dark)
    },

    toggle() {
      this.dark = !this.dark
      localStorage.setItem(STORAGE_KEY, this.dark ? 'dark' : 'light')
      applyToDocument(this.dark)
    },
  },
})
