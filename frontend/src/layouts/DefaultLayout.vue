<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useNotificationsStore } from '../stores/notifications'
import NotificationBell from '../components/NotificationBell.vue'
import ThemeToggle from '../components/ThemeToggle.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const notifications = useNotificationsStore()

onMounted(() => notifications.connect())
onUnmounted(() => notifications.disconnect())

const icons: Record<string, string> = {
  dashboard: 'M3 12l2-2m0 0 7-7 7 7M5 10v10a1 1 0 0 0 1 1h3m10-11 2 2m-2-2v10a1 1 0 0 1-1 1h-3m-6 0a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1m-6 0h6',
  customers: 'M17 20h5v-1a4 4 0 0 0-3-3.87M9 20H4v-1a4 4 0 0 1 3-3.87m5-4.13a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm6 0a4 4 0 1 0 0-8m0 8a4 4 0 0 1 0-8m-8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z',
  products: 'M21 8v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8m18 0-2-4H5L3 8m18 0H3m6 4h6',
  orders: 'M3 3h2l.4 2M7 13h10l3-8H5.4M7 13 5.4 5M7 13l-2.3 2.3A1 1 0 0 0 5.4 17H17M9 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2Zm8 0a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z',
  users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm11 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  'audit-log': 'M9 12h6m-6 4h6m2 5H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h6.5L18 7.5V19a2 2 0 0 1-2 2Z',
}

const links = computed(() => [
  { to: { name: 'dashboard' }, label: 'Dashboard' },
  { to: { name: 'customers' }, label: 'Clientes' },
  { to: { name: 'products' }, label: 'Produtos' },
  { to: { name: 'orders' }, label: 'Vendas' },
  ...(auth.role === 'ADMIN'
    ? [
        { to: { name: 'users' }, label: 'Usuários' },
        { to: { name: 'audit-log' }, label: 'Auditoria' },
      ]
    : []),
])

function isActiveLink(name: string) {
  return route.name === name
}

const initials = computed(() => {
  const role = auth.role ?? ''
  return role.slice(0, 2).toUpperCase()
})

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50 dark:bg-slate-950">
    <aside class="flex w-60 shrink-0 flex-col border-r border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
      <div class="flex items-center gap-2.5 px-5 py-5">
        <div
          class="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-brand-700 text-sm font-bold text-white shadow-soft"
        >
          N
        </div>
        <div class="min-w-0">
          <p class="truncate text-sm font-bold tracking-tight text-slate-900 dark:text-slate-50">Nexus</p>
          <p class="truncate text-xs text-slate-500 dark:text-slate-400">Tenant #{{ auth.tenantId }}</p>
        </div>
      </div>

      <nav class="flex flex-1 flex-col gap-0.5 px-3 py-2">
        <RouterLink
          v-for="link in links"
          :key="link.label"
          :to="link.to"
          class="group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors"
          :class="
            isActiveLink(link.to.name)
              ? 'bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-400'
              : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800'
          "
        >
          <span
            class="absolute -left-3 h-5 w-1 rounded-r-full bg-brand-600 transition-opacity"
            :class="isActiveLink(link.to.name) ? 'opacity-100' : 'opacity-0'"
          />
          <svg
            class="h-[18px] w-[18px] shrink-0"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <path stroke-linecap="round" stroke-linejoin="round" :d="icons[String(link.to.name)]" />
          </svg>
          {{ link.label }}
        </RouterLink>
      </nav>

      <div class="border-t border-slate-200 p-3 dark:border-slate-800">
        <div class="flex items-center gap-2.5 rounded-lg px-2 py-2">
          <div
            class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-600 dark:bg-slate-700 dark:text-slate-300"
          >
            {{ initials }}
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium text-slate-700 dark:text-slate-200">{{ auth.role }}</p>
          </div>
          <button
            class="shrink-0 rounded-md p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 dark:text-slate-500 dark:hover:bg-slate-800 dark:hover:text-slate-300"
            title="Sair"
            @click="handleLogout"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4m6 14 5-5-5-5m5 5H9"
              />
            </svg>
          </button>
        </div>
      </div>
    </aside>

    <div class="flex flex-1 flex-col">
      <header
        class="flex items-center justify-end gap-1 border-b border-slate-200 bg-white/80 px-6 py-3 backdrop-blur dark:border-slate-800 dark:bg-slate-900/80"
      >
        <ThemeToggle />
        <NotificationBell />
      </header>

      <main class="flex-1 p-6 lg:p-8">
        <div class="mx-auto max-w-6xl">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>
