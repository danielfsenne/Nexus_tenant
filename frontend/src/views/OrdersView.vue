<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import http from '../lib/http'
import type { Order, Customer } from '../types'

const orders = ref<Order[]>([])
const customers = ref<Customer[]>([])
const loading = ref(true)

const customerId = ref<number | null>(null)
const total = ref<number | null>(null)
const saving = ref(false)

const customerNameById = computed(() => {
  const map = new Map<number, string>()
  customers.value.forEach((c) => map.set(c.id, c.name))
  return map
})

async function loadData() {
  loading.value = true
  const [ordersRes, customersRes] = await Promise.all([
    http.get<Order[]>('/orders'),
    http.get<Customer[]>('/customers'),
  ])
  orders.value = ordersRes.data
  customers.value = customersRes.data
  loading.value = false
}

async function handleSubmit() {
  saving.value = true
  try {
    await http.post('/orders', { customerId: customerId.value, total: total.value })
    customerId.value = null
    total.value = null
    await loadData()
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
    <h1 class="mb-6 text-xl font-semibold text-slate-900">Vendas</h1>

    <form
      class="mb-6 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4"
      @submit.prevent="handleSubmit"
    >
      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">Cliente</label>
        <select
          v-model="customerId"
          required
          class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
        >
          <option :value="null" disabled>Selecione</option>
          <option v-for="customer in customers" :key="customer.id" :value="customer.id">
            {{ customer.name }}
          </option>
        </select>
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">Total</label>
        <input
          v-model.number="total"
          type="number"
          step="0.01"
          min="0"
          required
          class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
        />
      </div>

      <button
        type="submit"
        :disabled="saving"
        class="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
      >
        Registrar venda
      </button>
    </form>

    <div v-if="loading" class="text-sm text-slate-500">Carregando...</div>

    <table v-else class="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
      <thead class="bg-slate-50 text-left text-slate-500">
        <tr>
          <th class="px-4 py-2">Cliente</th>
          <th class="px-4 py-2">Total</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="order in orders" :key="order.id" class="border-t border-slate-100">
          <td class="px-4 py-2">{{ customerNameById.get(order.customerId) ?? `#${order.customerId}` }}</td>
          <td class="px-4 py-2">{{ formatCurrency(order.total) }}</td>
        </tr>
        <tr v-if="orders.length === 0">
          <td colspan="2" class="px-4 py-6 text-center text-slate-400">Nenhuma venda registrada.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
