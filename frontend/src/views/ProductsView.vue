<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../lib/http'
import type { Product } from '../types'

const products = ref<Product[]>([])
const loading = ref(true)

const name = ref('')
const price = ref<number | null>(null)
const editingId = ref<number | null>(null)
const saving = ref(false)

async function loadProducts() {
  loading.value = true
  const { data } = await http.get<Product[]>('/products')
  products.value = data
  loading.value = false
}

async function handleSubmit() {
  saving.value = true
  try {
    const payload = { name: name.value, price: price.value }
    if (editingId.value) {
      await http.put(`/products/${editingId.value}`, payload)
    } else {
      await http.post('/products', payload)
    }
    resetForm()
    await loadProducts()
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
    <h1 class="mb-6 text-xl font-semibold text-slate-900">Produtos</h1>

    <form
      class="mb-6 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4"
      @submit.prevent="handleSubmit"
    >
      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">Nome</label>
        <input
          v-model="name"
          required
          class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
        />
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">Preço</label>
        <input
          v-model.number="price"
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
        {{ editingId ? 'Salvar' : 'Adicionar' }}
      </button>

      <button
        v-if="editingId"
        type="button"
        class="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
        @click="resetForm"
      >
        Cancelar
      </button>
    </form>

    <div v-if="loading" class="text-sm text-slate-500">Carregando...</div>

    <table v-else class="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
      <thead class="bg-slate-50 text-left text-slate-500">
        <tr>
          <th class="px-4 py-2">Nome</th>
          <th class="px-4 py-2">Preço</th>
          <th class="px-4 py-2"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="product in products" :key="product.id" class="border-t border-slate-100">
          <td class="px-4 py-2">{{ product.name }}</td>
          <td class="px-4 py-2">{{ formatCurrency(product.price) }}</td>
          <td class="px-4 py-2 text-right">
            <button class="text-slate-600 hover:underline" @click="editProduct(product)">Editar</button>
            <button class="ml-3 text-red-600 hover:underline" @click="deleteProduct(product.id)">Excluir</button>
          </td>
        </tr>
        <tr v-if="products.length === 0">
          <td colspan="3" class="px-4 py-6 text-center text-slate-400">Nenhum produto cadastrado.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
