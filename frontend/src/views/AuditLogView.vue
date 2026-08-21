<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../lib/http'
import type { AuditLog } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import Spinner from '../components/ui/Spinner.vue'

const logs = ref<AuditLog[]>([])
const loading = ref(true)

const actionLabels: Record<AuditLog['action'], string> = {
  CREATED: 'Criou',
  UPDATED: 'Atualizou',
  DELETED: 'Excluiu',
  INVITED: 'Convidou',
  INVITE_ACCEPTED: 'Convite aceito',
  PROCESSED: 'Processou',
  PROCESSING_FAILED: 'Falhou ao processar',
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
    <PageHeader title="Auditoria" />

    <Spinner v-if="loading" />

    <BaseCard v-else class="overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-slate-500 dark:bg-slate-900/40 dark:text-slate-400">
          <tr>
            <th class="px-4 py-2">Quando</th>
            <th class="px-4 py-2">Usuário</th>
            <th class="px-4 py-2">Ação</th>
            <th class="px-4 py-2">Detalhes</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id" class="border-t border-slate-100 dark:border-slate-700">
            <td class="whitespace-nowrap px-4 py-2 text-slate-500 dark:text-slate-400">
              {{ formatDateTime(log.createdAt) }}
            </td>
            <td class="px-4 py-2 text-slate-900 dark:text-slate-100">{{ log.userEmail ?? '—' }}</td>
            <td class="px-4 py-2 text-slate-700 dark:text-slate-300">
              {{ actionLabels[log.action] }} {{ entityLabels[log.entityType] ?? log.entityType.toLowerCase() }}
            </td>
            <td class="px-4 py-2 text-slate-500 dark:text-slate-400">{{ log.details ?? '—' }}</td>
          </tr>
        </tbody>
      </table>

      <EmptyState v-if="logs.length === 0" message="Nenhum evento registrado ainda." />
    </BaseCard>
  </div>
</template>
