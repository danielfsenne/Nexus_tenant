<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../lib/http'
import type { Customer } from '../types'

const customers = ref<Customer[]>([])
const loading = ref(true)

const name = ref('')
const email = ref('')
const editingId = ref<number | null>(null)
const saving = ref(false)

async function loadCustomers() {
  loading.value = true
  const { data } = await http.get<Customer[]>('/customers')
  customers.value = data
  loading.value = false
}

async function handleSubmit() {
  saving.value = true
  try {
    if (editingId.value) {
      await http.put(`/customers/${editingId.value}`, { name: name.value, email: email.value })
    } else {
      await http.post('/customers', { name: name.value, email: email.value })
    }
    resetForm()
    await loadCustomers()
  } finally {
    saving.value = false
  }
}

function editCustomer(customer: Customer) {
  editingId.value = customer.id
  name.value = customer.name
  email.value = customer.email ?? ''
}

function resetForm() {
  editingId.value = null
  name.value = ''
  email.value = ''
}

async function deleteCustomer(id: number) {
  await http.delete(`/customers/${id}`)
  await loadCustomers()
}

onMounted(loadCustomers)
</script>

<template>
  <div>
    <h1 class="mb-6 text-xl font-semibold text-slate-900">Clientes</h1>

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
        <label class="mb-1 block text-sm font-medium text-slate-700">E-mail</label>
        <input
          v-model="email"
          type="email"
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
          <th class="px-4 py-2">E-mail</th>
          <th class="px-4 py-2"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="customer in customers" :key="customer.id" class="border-t border-slate-100">
          <td class="px-4 py-2">{{ customer.name }}</td>
          <td class="px-4 py-2">{{ customer.email }}</td>
          <td class="px-4 py-2 text-right">
            <button class="text-slate-600 hover:underline" @click="editCustomer(customer)">Editar</button>
            <button class="ml-3 text-red-600 hover:underline" @click="deleteCustomer(customer.id)">Excluir</button>
          </td>
        </tr>
        <tr v-if="customers.length === 0">
          <td colspan="3" class="px-4 py-6 text-center text-slate-400">Nenhum cliente cadastrado.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
