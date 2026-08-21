<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useNotificationsStore } from '../stores/notifications'
import NotificationBell from '../components/NotificationBell.vue'

const auth = useAuthStore()
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

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50">
    <aside class="w-56 shrink-0 border-r border-slate-200 bg-white">
      <div class="border-b border-slate-200 px-4 py-4">
        <p class="text-lg font-semibold text-slate-900">Nexus</p>
        <p class="text-xs text-slate-500">Tenant #{{ auth.tenantId }} · {{ auth.role }}</p>
      </div>

      <nav class="flex flex-col gap-1 p-3">
        <RouterLink
          v-for="link in links"
          :key="link.label"
          :to="link.to"
          class="rounded-md px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
          active-class="bg-slate-900 text-white hover:bg-slate-900"
        >
          {{ link.label }}
        </RouterLink>
      </nav>

      <div class="mt-auto p-3">
        <button
          class="w-full rounded-md px-3 py-2 text-left text-sm font-medium text-slate-600 hover:bg-slate-100"
          @click="handleLogout"
        >
          Sair
        </button>
      </div>
    </aside>

    <div class="flex flex-1 flex-col">
      <header class="flex justify-end border-b border-slate-200 bg-white px-6 py-3">
        <NotificationBell />
      </header>

      <main class="flex-1 p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
