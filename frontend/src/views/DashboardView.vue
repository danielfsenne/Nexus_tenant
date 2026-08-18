<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import http from '../lib/http'
import type { Customer, Product, Order } from '../types'

const customers = ref<Customer[]>([])
const products = ref<Product[]>([])
const orders = ref<Order[]>([])
const loading = ref(true)

const totalSales = computed(() =>
  orders.value.reduce((sum, order) => sum + Number(order.total), 0),
)

onMounted(async () => {
  try {
    const [customersRes, productsRes, ordersRes] = await Promise.all([
      http.get<Customer[]>('/customers'),
      http.get<Product[]>('/products'),
      http.get<Order[]>('/orders'),
    ])
    customers.value = customersRes.data
    products.value = productsRes.data
    orders.value = ordersRes.data
  } finally {
    loading.value = false
  }
})

function formatCurrency(value: number) {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}
</script>

<template>
  <div>
    <h1 class="mb-6 text-xl font-semibold text-slate-900">Dashboard</h1>

    <div v-if="loading" class="text-sm text-slate-500">Carregando...</div>

    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <div class="rounded-lg border border-slate-200 bg-white p-5">
        <p class="text-sm text-slate-500">Clientes</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900">{{ customers.length }}</p>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white p-5">
        <p class="text-sm text-slate-500">Produtos</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900">{{ products.length }}</p>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white p-5">
        <p class="text-sm text-slate-500">Vendas</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900">{{ formatCurrency(totalSales) }}</p>
      </div>
    </div>
  </div>
</template>
