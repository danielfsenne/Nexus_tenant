<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import http, { extractErrorMessage } from '../lib/http'
import { useToastStore } from '../stores/toast'
import type { Order, Customer } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseSelect from '../components/ui/BaseSelect.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import Spinner from '../components/ui/Spinner.vue'

const toast = useToastStore()

const orders = ref<Order[]>([])
const customers = ref<Customer[]>([])
const loading = ref(true)

const customerId = ref<number | null>(null)
const total = ref<number | null>(null)
const saving = ref(false)
const errorMessage = ref('')

const customerNameById = computed(() => {
  const map = new Map<number, string>()
  customers.value.forEach((c) => map.set(c.id, c.name))
  return map
})

async function loadData() {
  loading.value = true
  try {
    const [ordersRes, customersRes] = await Promise.all([
      http.get<Order[]>('/orders'),
      http.get<Customer[]>('/customers'),
    ])
    orders.value = ordersRes.data
    customers.value = customersRes.data
  } catch {
    // erro de rede/autenticação já é tratado pelo interceptor global
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  saving.value = true
  errorMessage.value = ''
  try {
    await http.post('/orders', { customerId: Number(customerId.value), total: total.value })
    customerId.value = null
    total.value = null
    await loadData()
    toast.success('Venda registrada.')
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível registrar a venda.')
  } finally {
    saving.value = false
  }
}

function formatCurrency(value: number) {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

onMounted(loadData)
</script>

<template>
  <div>
    <PageHeader title="Vendas" />

    <BaseCard class="mb-6 p-4">
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="handleSubmit">
        <div class="min-w-48">
          <BaseSelect v-model="customerId" label="Cliente" required>
            <option :value="null" disabled>Selecione</option>
            <option v-for="customer in customers" :key="customer.id" :value="customer.id">
              {{ customer.name }}
            </option>
          </BaseSelect>
        </div>

        <div class="w-32">
          <BaseInput v-model.number="total" label="Total" type="number" step="0.01" min="0" required />
        </div>

        <BaseButton type="submit" :loading="saving">Registrar venda</BaseButton>

        <Alert v-if="errorMessage" variant="error" class="w-full">{{ errorMessage }}</Alert>
      </form>
    </BaseCard>

    <Spinner v-if="loading" />

    <BaseCard v-else class="overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:bg-slate-800/60 dark:text-slate-400">
          <tr>
            <th class="px-4 py-3">Cliente</th>
            <th class="px-4 py-3">Total</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="order in orders"
            :key="order.id"
            class="border-t border-slate-100 transition-colors hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
          >
            <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">
              {{ customerNameById.get(order.customerId) ?? `#${order.customerId}` }}
            </td>
            <td class="px-4 py-3 text-slate-600 dark:text-slate-400">{{ formatCurrency(order.total) }}</td>
          </tr>
        </tbody>
      </table>

      <EmptyState v-if="orders.length === 0" message="Nenhuma venda registrada." />
    </BaseCard>
  </div>
</template>
