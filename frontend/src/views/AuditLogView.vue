<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import http from '../lib/http'
import type { AuditLog, PageResponse } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseSelect from '../components/ui/BaseSelect.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import Spinner from '../components/ui/Spinner.vue'
import Pagination from '../components/ui/Pagination.vue'

const logs = ref<AuditLog[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(true)
const filterAction = ref<AuditLog['action'] | ''>('')

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

async function loadLogs() {
  loading.value = true
  try {
    const { data } = await http.get<PageResponse<AuditLog>>('/audit-logs', {
      params: { page: page.value, size: 20, action: filterAction.value || undefined },
    })
    logs.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch {
    // erro de rede/autenticação já é tratado pelo interceptor global
  } finally {
    loading.value = false
  }
}

function changePage(newPage: number) {
  page.value = newPage
  loadLogs()
}

watch(filterAction, () => {
  page.value = 0
  loadLogs()
})

onMounted(loadLogs)
</script>

<template>
  <div>
    <PageHeader title="Auditoria" />

    <div class="mb-4 max-w-xs">
      <BaseSelect v-model="filterAction">
        <option value="">Todas as ações</option>
        <option v-for="(label, action) in actionLabels" :key="action" :value="action">{{ label }}</option>
      </BaseSelect>
    </div>

    <Spinner v-if="loading" />

    <BaseCard v-else class="overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:bg-slate-800/60 dark:text-slate-400">
          <tr>
            <th class="px-4 py-3">Quando</th>
            <th class="px-4 py-3">Usuário</th>
            <th class="px-4 py-3">Ação</th>
            <th class="px-4 py-3">Detalhes</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="log in logs"
            :key="log.id"
            class="border-t border-slate-100 transition-colors hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
          >
            <td class="whitespace-nowrap px-4 py-3 text-slate-500 dark:text-slate-400">
              {{ formatDateTime(log.createdAt) }}
            </td>
            <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{{ log.userEmail ?? '—' }}</td>
            <td class="px-4 py-3 text-slate-700 dark:text-slate-300">
              {{ actionLabels[log.action] }} {{ entityLabels[log.entityType] ?? log.entityType.toLowerCase() }}
            </td>
            <td class="px-4 py-3 text-slate-500 dark:text-slate-400">{{ log.details ?? '—' }}</td>
          </tr>
        </tbody>
      </table>

      <EmptyState
        v-if="logs.length === 0"
        :message="filterAction ? 'Nenhum evento encontrado para esse filtro.' : 'Nenhum evento registrado ainda.'"
      />

      <Pagination :page="page" :total-pages="totalPages" :total-elements="totalElements" @update:page="changePage" />
    </BaseCard>
  </div>
</template>
