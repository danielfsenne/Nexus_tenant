<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import http from '../lib/http'
import UsageBar from '../components/UsageBar.vue'
import PageHeader from '../components/ui/PageHeader.vue'
import StatCard from '../components/ui/StatCard.vue'
import Spinner from '../components/ui/Spinner.vue'
import type { Order, PageResponse, TenantUsage } from '../types'

const customerCount = ref(0)
const productCount = ref(0)
const orders = ref<Order[]>([])
const tenantUsage = ref<TenantUsage | null>(null)
const loading = ref(true)

const totalSales = computed(() =>
  orders.value.reduce((sum, order) => sum + Number(order.total), 0),
)

onMounted(async () => {
  try {
    const [customersRes, productsRes, ordersRes, usageRes] = await Promise.all([
      http.get<PageResponse<unknown>>('/customers', { params: { page: 0, size: 1 } }),
      http.get<PageResponse<unknown>>('/products', { params: { page: 0, size: 1 } }),
      http.get<PageResponse<Order>>('/orders', { params: { page: 0, size: 1000 } }),
      http.get<TenantUsage>('/tenants/me'),
    ])
    customerCount.value = customersRes.data.totalElements
    productCount.value = productsRes.data.totalElements
    orders.value = ordersRes.data.content
    tenantUsage.value = usageRes.data
  } catch {
    // erro de rede/autenticação já é tratado pelo interceptor global
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
    <PageHeader title="Dashboard" :subtitle="tenantUsage ? tenantUsage.companyName : undefined" />

    <Spinner v-if="loading" />

    <template v-else>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Clientes" :value="customerCount" accent="brand" />
        <StatCard label="Produtos" :value="productCount" accent="emerald" />
        <StatCard label="Vendas" :value="formatCurrency(totalSales)" accent="amber" />
      </div>

      <div
        v-if="tenantUsage"
        class="mt-6 rounded-xl border border-slate-200/80 bg-white p-5 shadow-softer dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-4 flex items-center justify-between">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">Uso do plano</h2>
          <span class="rounded-full bg-brand-600 px-3 py-1 text-xs font-semibold text-white shadow-sm">
            {{ tenantUsage.plan }}
          </span>
        </div>

        <div class="space-y-4">
          <UsageBar label="Usuários" :current="tenantUsage.usage.users" :max="tenantUsage.limits.maxUsers" />
          <UsageBar label="Clientes" :current="tenantUsage.usage.customers" :max="tenantUsage.limits.maxCustomers" />
          <UsageBar label="Produtos" :current="tenantUsage.usage.products" :max="tenantUsage.limits.maxProducts" />
        </div>
      </div>
    </template>
  </div>
</template>
