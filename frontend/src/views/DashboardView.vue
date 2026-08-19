<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import http from '../lib/http'
import UsageBar from '../components/UsageBar.vue'
import type { Customer, Product, Order, TenantUsage } from '../types'

const customers = ref<Customer[]>([])
const products = ref<Product[]>([])
const orders = ref<Order[]>([])
const tenantUsage = ref<TenantUsage | null>(null)
const loading = ref(true)

const totalSales = computed(() =>
  orders.value.reduce((sum, order) => sum + Number(order.total), 0),
)

onMounted(async () => {
  try {
    const [customersRes, productsRes, ordersRes, usageRes] = await Promise.all([
      http.get<Customer[]>('/customers'),
      http.get<Product[]>('/products'),
      http.get<Order[]>('/orders'),
      http.get<TenantUsage>('/tenants/me'),
    ])
    customers.value = customersRes.data
    products.value = productsRes.data
    orders.value = ordersRes.data
    tenantUsage.value = usageRes.data
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

    <div v-if="tenantUsage" class="mt-6 rounded-lg border border-slate-200 bg-white p-5">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-sm font-semibold text-slate-900">Uso do plano</h2>
        <span class="rounded-full bg-slate-900 px-3 py-1 text-xs font-medium text-white">
          {{ tenantUsage.plan }}
        </span>
      </div>

      <div class="space-y-4">
        <UsageBar label="Usuários" :current="tenantUsage.usage.users" :max="tenantUsage.limits.maxUsers" />
        <UsageBar label="Clientes" :current="tenantUsage.usage.customers" :max="tenantUsage.limits.maxCustomers" />
        <UsageBar label="Produtos" :current="tenantUsage.usage.products" :max="tenantUsage.limits.maxProducts" />
      </div>
    </div>
  </div>
</template>
