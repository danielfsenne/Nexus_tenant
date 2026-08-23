<script setup lang="ts">
import { ref } from 'vue'
import { onClickOutside } from '../lib/onClickOutside'
import { useNotificationsStore } from '../stores/notifications'

const notifications = useNotificationsStore()
const open = ref(false)
const rootRef = ref<HTMLElement | null>(null)

onClickOutside(rootRef, () => {
  open.value = false
})

function toggle() {
  open.value = !open.value
  if (open.value) {
    notifications.markAllRead()
  }
}

function formatTime(value: string) {
  return new Date(value).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div ref="rootRef" class="relative">
    <button
      class="relative rounded-lg p-2 text-slate-500 transition-colors hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-700"
      :title="notifications.connected ? 'Conectado' : 'Desconectado'"
      @click="toggle"
    >
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="h-5 w-5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15 17h5l-1.4-1.4A2 2 0 0 1 18 14.2V11a6 6 0 1 0-12 0v3.2a2 2 0 0 1-.6 1.4L4 17h5m6 0v1a3 3 0 1 1-6 0v-1m6 0H9" />
      </svg>

      <span
        v-if="notifications.unreadCount > 0"
        class="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-medium text-white"
      >
        {{ notifications.unreadCount > 9 ? '9+' : notifications.unreadCount }}
      </span>

      <span
        class="absolute bottom-1 right-1 h-1.5 w-1.5 rounded-full"
        :class="notifications.connected ? 'bg-emerald-500' : 'bg-slate-300'"
      />
    </button>

    <div
      v-if="open"
      class="absolute right-0 z-10 mt-2 w-80 rounded-xl border border-slate-200/80 bg-white shadow-soft dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="border-b border-slate-100 px-4 py-3 text-sm font-semibold text-slate-900 dark:border-slate-800 dark:text-slate-50">
        Notificações
      </div>

      <div class="max-h-80 overflow-y-auto">
        <p v-if="notifications.items.length === 0" class="px-4 py-6 text-center text-sm text-slate-400 dark:text-slate-500">
          Nenhuma notificação ainda.
        </p>

        <div
          v-for="(item, index) in notifications.items"
          :key="index"
          class="border-b border-slate-50 px-4 py-3 last:border-b-0 dark:border-slate-700/50"
        >
          <p class="text-sm text-slate-700 dark:text-slate-200">{{ item.message }}</p>
          <p class="mt-0.5 text-xs text-slate-400 dark:text-slate-500">{{ formatTime(item.createdAt) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
