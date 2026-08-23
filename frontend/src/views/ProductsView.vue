<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http, { extractErrorMessage } from '../lib/http'
import type { Product } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import Spinner from '../components/ui/Spinner.vue'

const products = ref<Product[]>([])
const loading = ref(true)

const name = ref('')
const price = ref<number | null>(null)
const editingId = ref<number | null>(null)
const saving = ref(false)
const errorMessage = ref('')

async function loadProducts() {
  loading.value = true
  const { data } = await http.get<Product[]>('/products')
  products.value = data
  loading.value = false
}

async function handleSubmit() {
  saving.value = true
  errorMessage.value = ''
  try {
    const payload = { name: name.value, price: price.value }
    if (editingId.value) {
      await http.put(`/products/${editingId.value}`, payload)
    } else {
      await http.post('/products', payload)
    }
    resetForm()
    await loadProducts()
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível salvar o produto.')
  } finally {
    saving.value = false
  }
}

function editProduct(product: Product) {
  editingId.value = product.id
  name.value = product.name
  price.value = product.price
}

function resetForm() {
  editingId.value = null
  name.value = ''
  price.value = null
}

async function deleteProduct(id: number) {
  await http.delete(`/products/${id}`)
  await loadProducts()
}

function formatCurrency(value: number) {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

onMounted(loadProducts)
</script>

<template>
  <div>
    <PageHeader title="Produtos" />

    <BaseCard class="mb-6 p-4">
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="handleSubmit">
        <div class="min-w-48">
          <BaseInput v-model="name" label="Nome" required />
        </div>
        <div class="w-32">
          <BaseInput v-model.number="price" label="Preço" type="number" step="0.01" min="0" required />
        </div>

        <BaseButton type="submit" :loading="saving">
          {{ editingId ? 'Salvar' : 'Adicionar' }}
        </BaseButton>

        <BaseButton v-if="editingId" type="button" variant="secondary" @click="resetForm">
          Cancelar
        </BaseButton>

        <Alert v-if="errorMessage" variant="error" class="w-full">{{ errorMessage }}</Alert>
      </form>
    </BaseCard>

    <Spinner v-if="loading" />

    <BaseCard v-else class="overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:bg-slate-800/60 dark:text-slate-400">
          <tr>
            <th class="px-4 py-3">Nome</th>
            <th class="px-4 py-3">Preço</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="product in products"
            :key="product.id"
            class="border-t border-slate-100 transition-colors hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
          >
            <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{{ product.name }}</td>
            <td class="px-4 py-3 text-slate-600 dark:text-slate-400">{{ formatCurrency(product.price) }}</td>
            <td class="px-4 py-3 text-right">
              <button class="font-medium text-brand-600 hover:underline dark:text-brand-400" @click="editProduct(product)">
                Editar
              </button>
              <button class="ml-3 font-medium text-red-600 hover:underline dark:text-red-400" @click="deleteProduct(product.id)">
                Excluir
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <EmptyState v-if="products.length === 0" message="Nenhum produto cadastrado." />
    </BaseCard>
  </div>
</template>
