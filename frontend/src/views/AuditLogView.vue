<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../lib/http'
import type { AuditLog } from '../types'

const logs = ref<AuditLog[]>([])
const loading = ref(true)

const actionLabels: Record<AuditLog['action'], string> = {
  CREATED: 'Criou',
  UPDATED: 'Atualizou',
  DELETED: 'Excluiu',
  INVITED: 'Convidou',
  INVITE_ACCEPTED: 'Convite aceito',
}

const entityLabels: Record<string, string> = {
  CUSTOMER: 'cliente',
  PRODUCT: 'produto',
  ORDER: 'venda',
  USER: 'usuário',
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString('pt-BR')
}

onMounted(async () => {
  const { data } = await http.get<AuditLog[]>('/audit-logs')
  logs.value = data
  loading.value = false
})
</script>

<template>
  <div>
    <h1 class="mb-6 text-xl font-semibold text-slate-900">Auditoria</h1>

    <div v-if="loading" class="text-sm text-slate-500">Carregando...</div>

    <table v-else class="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
      <thead class="bg-slate-50 text-left text-slate-500">
        <tr>
          <th class="px-4 py-2">Quando</th>
          <th class="px-4 py-2">Usuário</th>
          <th class="px-4 py-2">Ação</th>
          <th class="px-4 py-2">Detalhes</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="log in logs" :key="log.id" class="border-t border-slate-100">
          <td class="px-4 py-2 whitespace-nowrap text-slate-500">{{ formatDateTime(log.createdAt) }}</td>
          <td class="px-4 py-2">{{ log.userEmail ?? '—' }}</td>
          <td class="px-4 py-2">
            {{ actionLabels[log.action] }} {{ entityLabels[log.entityType] ?? log.entityType.toLowerCase() }}
          </td>
          <td class="px-4 py-2 text-slate-500">{{ log.details ?? '—' }}</td>
        </tr>
        <tr v-if="logs.length === 0">
          <td colspan="4" class="px-4 py-6 text-center text-slate-400">Nenhum evento registrado ainda.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
