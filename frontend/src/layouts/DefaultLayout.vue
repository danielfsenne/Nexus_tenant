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

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50 dark:bg-slate-900">
    <aside class="w-56 shrink-0 border-r border-slate-200 bg-white dark:border-slate-700 dark:bg-slate-800">
      <div class="border-b border-slate-200 px-4 py-4 dark:border-slate-700">
        <p class="text-lg font-semibold text-slate-900 dark:text-slate-50">Nexus</p>
        <p class="text-xs text-slate-500 dark:text-slate-400">Tenant #{{ auth.tenantId }} · {{ auth.role }}</p>
      </div>

      <nav class="flex flex-col gap-1 p-3">
        <RouterLink
          v-for="link in links"
          :key="link.label"
          :to="link.to"
          class="rounded-md px-3 py-2 text-sm font-medium transition-colors"
          :class="
            isActiveLink(link.to.name)
              ? 'bg-brand-600 text-white'
              : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-700'
          "
        >
          {{ link.label }}
        </RouterLink>
      </nav>

      <div class="mt-auto p-3">
        <button
          class="w-full rounded-md px-3 py-2 text-left text-sm font-medium text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-700"
          @click="handleLogout"
        >
          Sair
        </button>
      </div>
    </aside>

    <div class="flex flex-1 flex-col">
      <header class="flex justify-end gap-1 border-b border-slate-200 bg-white px-6 py-3 dark:border-slate-700 dark:bg-slate-800">
        <ThemeToggle />
        <NotificationBell />
      </header>

      <main class="flex-1 p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
